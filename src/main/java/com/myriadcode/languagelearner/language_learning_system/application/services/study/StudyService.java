package com.myriadcode.languagelearner.language_learning_system.application.services.study;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.study.response.StudySessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.*;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudyService {

    private static final int FIXED_SESSION_SIZE = 1;

    private final StudySessionJpaRepo sessionRepo;
    private final StudySessionItemJpaRepo itemRepo;
    private final StudySentencePoolJpaRepo sentencePoolRepo;
    private final StudyUserSentenceUsageJpaRepo usageRepo;
    private final StudyAnswerLogJpaRepo answerLogRepo;
    private final PracticeVocabularyReferenceRepo practiceRepo;
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final VocabularyRepo vocabularyRepo;
    private final VocabularyClozeLlmApi vocabularyClozeLlmApi;
    private final StudyAnswerEvaluationLlmApi studyAnswerEvaluationLlmApi;
    private final ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi;

    public StudyService(StudySessionJpaRepo sessionRepo,
                        StudySessionItemJpaRepo itemRepo,
                        StudySentencePoolJpaRepo sentencePoolRepo,
                        StudyUserSentenceUsageJpaRepo usageRepo,
                        StudyAnswerLogJpaRepo answerLogRepo,
                        PracticeVocabularyReferenceRepo practiceRepo,
                        FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                        VocabularyRepo vocabularyRepo,
                        VocabularyClozeLlmApi vocabularyClozeLlmApi,
                        StudyAnswerEvaluationLlmApi studyAnswerEvaluationLlmApi,
                        ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi) {
        this.sessionRepo = sessionRepo;
        this.itemRepo = itemRepo;
        this.sentencePoolRepo = sentencePoolRepo;
        this.usageRepo = usageRepo;
        this.answerLogRepo = answerLogRepo;
        this.practiceRepo = practiceRepo;
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.vocabularyRepo = vocabularyRepo;
        this.vocabularyClozeLlmApi = vocabularyClozeLlmApi;
        this.studyAnswerEvaluationLlmApi = studyAnswerEvaluationLlmApi;
        this.reviewVocabularyFlashcardApi = reviewVocabularyFlashcardApi;
    }

    @Transactional
    public StudySessionResponse createSession(String userId) {
        var existing = sessionRepo.findFirstByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            return toResponse(existing, null, null);
        }

        var ranked = rankCandidates(userId);
        if (ranked.isEmpty()) {
            throw new IllegalArgumentException("No practice vocabulary found for study");
        }

        var session = new StudySessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setStatus("ACTIVE");
        session.setCreatedAt(Instant.now());
        sessionRepo.save(session);

        var usedIds = usageRepo.findAllByUserId(userId).stream().map(StudyUserSentenceUsageEntity::getSentenceId).collect(Collectors.toSet());
        int rank = 0;
        for (var candidate : ranked) {
            if (rank >= FIXED_SESSION_SIZE) break;
            var sentence = selectOrGenerateSentence(candidate.vocabulary(), userId, usedIds);
            if (sentence == null) continue;
            markShown(userId, sentence.getId());

            var item = new StudySessionItemEntity();
            item.setId(UUID.randomUUID().toString());
            item.setSessionId(session.getId());
            item.setFlashcardId(candidate.review().flashcardId());
            item.setVocabularyId(candidate.vocabulary().id().id());
            item.setSentenceId(sentence.getId());
            item.setQueueRankSnapshot(rank);
            item.setPresentedAt(Instant.now());
            itemRepo.save(item);
            rank++;
        }

        if (itemRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc(session.getId()).isEmpty()) {
            throw new IllegalArgumentException("Unable to create study items");
        }
        return toResponse(session, null, null);
    }

    @Transactional(readOnly = true)
    public StudySessionResponse getActiveSession(String userId) {
        var session = sessionRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("No study session found"));
        if (!"ACTIVE".equals(session.getStatus())) {
            throw new IllegalArgumentException("No active study session found");
        }
        return toResponse(session, null, null);
    }

    @Transactional
    public StudySessionResponse submitAnswer(String sessionId, String itemId, String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("answer is required");
        }
        var session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Study session not found"));
        if (!Objects.equals(session.getUserId(), userId)) {
            throw new IllegalArgumentException("Study session not found");
        }
        var item = itemRepo.findByIdAndSessionId(itemId, sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Study item not found"));
        if (item.getRatedAt() != null) {
            throw new IllegalArgumentException("Study item already rated");
        }

        var sentence = sentencePoolRepo.findById(item.getSentenceId())
                .orElseThrow(() -> new IllegalArgumentException("Study sentence not found"));

        var normalizedAnswer = normalize(answer);
        boolean exact = normalizedAnswer.equals(sentence.getNormalizedTrueAnswer());
        Rating appliedRating;
        String feedback;
        String evalMode;
        String llmPayload;

        if (exact) {
            appliedRating = Rating.GOOD;
            feedback = "Correct answer.";
            evalMode = "EXACT";
            llmPayload = null;
        } else {
            com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationResult eval;
            try (var ignored = LlmUserContextHolder.scoped(userId)) {
                eval = studyAnswerEvaluationLlmApi.evaluate(
                        sentence.getSentenceTextWithBlank(),
                        sentence.getTrueAnswerSurface(),
                        "",
                        sentence.getHint(),
                        answer
                );
            }
            appliedRating = mapRating(eval.semanticMatch(), eval.formAccuracy(), eval.confidence());
            feedback = eval.feedback();
            evalMode = "LLM";
            llmPayload = "{\"semanticMatch\":" + eval.semanticMatch() + ",\"formAccuracy\":" + eval.formAccuracy() + ",\"confidence\":" + eval.confidence() + "}";
        }

        reviewVocabularyFlashcardApi.reviewVocabularyFlashcard(item.getFlashcardId(), appliedRating);

        item.setRatedAt(Instant.now());
        item.setRatingApplied(appliedRating.name());
        item.setAnswerText(answer);
        item.setEvaluationMode(evalMode);
        item.setFeedbackText(feedback);
        itemRepo.save(item);

        var usage = usageRepo.findByUserIdAndSentenceId(userId, sentence.getId()).orElse(null);
        if (usage != null) {
            usage.setLastSeenAt(Instant.now());
            if (appliedRating == Rating.GOOD) {
                usage.setTimesCorrect(usage.getTimesCorrect() + 1);
            } else {
                usage.setTimesWrong(usage.getTimesWrong() + 1);
            }
            usageRepo.save(usage);
        }

        var log = new StudyAnswerLogEntity();
        log.setId(UUID.randomUUID().toString());
        log.setSessionItemId(item.getId());
        log.setUserAnswer(answer);
        log.setNormalizedUserAnswer(normalizedAnswer);
        log.setExactMatch(exact);
        log.setLlmPayloadJson(llmPayload);
        log.setMappedRating(appliedRating.name());
        log.setCreatedAt(Instant.now());
        answerLogRepo.save(log);

        var items = itemRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc(sessionId);
        boolean completed = items.stream().allMatch(value -> value.getRatedAt() != null);
        if (completed) {
            session.setStatus("COMPLETED");
            session.setCompletedAt(Instant.now());
            sessionRepo.save(session);
        }

        return toResponse(session, feedback, appliedRating);
    }

    private StudySessionResponse toResponse(StudySessionEntity session, String feedback, Rating appliedRating) {
        var items = itemRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc(session.getId());
        int total = items.size();
        int rated = (int) items.stream().filter(item -> item.getRatedAt() != null).count();
        var current = items.stream().filter(item -> item.getRatedAt() == null).findFirst().orElse(null);
        StudySessionResponse.Item currentItem = null;
        if (current != null) {
            var sentence = sentencePoolRepo.findById(current.getSentenceId()).orElse(null);
            currentItem = new StudySessionResponse.Item(
                    current.getId(),
                    current.getFlashcardId(),
                    current.getVocabularyId(),
                    current.getSentenceId(),
                    sentence == null ? "" : sentence.getSentenceTextWithBlank(),
                    sentence == null ? "" : sentence.getHint(),
                    sentence == null ? "" : sentence.getTrueAnswerSurface(),
                    ""
            );
        }
        return new StudySessionResponse(session.getId(), session.getStatus(), rated, total, currentItem, session.getCreatedAt(), feedback, appliedRating);
    }

    private List<StudyCandidate> rankCandidates(String userId) {
        var refs = practiceRepo.findByUserId(userId);
        if (refs.isEmpty()) return List.of();

        var vocabIds = refs.stream().map(ref -> ref.vocabularyId().id()).distinct().toList();
        var vocabMap = vocabularyRepo.findByIds(vocabIds).stream()
                .filter(v -> v.userId() != null && userId.equals(v.userId().id()))
                .collect(Collectors.toMap(v -> v.id().id(), v -> v));

        var reviews = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(VocabularyFlashcardReviewRecord::isReversed)
                .filter(review -> vocabMap.containsKey(review.vocabularyId()))
                .toList();

        return reviews.stream()
                .map(review -> new StudyCandidate(review, vocabMap.get(review.vocabularyId())))
                .sorted(Comparator
                        .comparing((StudyCandidate c) -> dueBucket(c.review(), Instant.now()))
                        .thenComparing(c -> overdueDurationOrZero(c.review(), Instant.now()), Comparator.reverseOrder())
                        .thenComparing(c -> retrievabilityOrMax(c.review()))
                        .thenComparing(c -> timeUntilDueOrMax(c.review(), Instant.now()))
                        .thenComparing(c -> c.review().lastReview() == null ? Instant.EPOCH : c.review().lastReview())
                        .thenComparing(c -> c.review().lapses(), Comparator.reverseOrder())
                        .thenComparing(c -> statePriority(c.review().fsrsState()))
                )
                .toList();
    }

    private StudySentencePoolEntity selectOrGenerateSentence(Vocabulary vocabulary, String userId, Set<String> alreadyUsed) {
        var existing = sentencePoolRepo.findAllByVocabularyIdOrderByCreatedAtAsc(vocabulary.id().id()).stream()
                .filter(sentence -> !alreadyUsed.contains(sentence.getId()))
                .findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }

        var seed = new VocabularyClozeGenerationSeed(vocabulary.id().id(), vocabulary.surface(), vocabulary.translation());
        List<com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult> generated;
        try (var ignored = LlmUserContextHolder.scoped(userId)) {
            generated = vocabularyClozeLlmApi.generateClozeSentences("Practice", List.of(seed));
        }
        if (generated == null || generated.isEmpty()) {
            return null;
        }
        var first = generated.getFirst();
        if (first.clozeText() == null || first.clozeText().isBlank() || first.answerText() == null || first.answerText().isBlank()) {
            return null;
        }

        var sentence = new StudySentencePoolEntity();
        sentence.setId(UUID.randomUUID().toString());
        sentence.setVocabularyId(vocabulary.id().id());
        sentence.setSentenceTextWithBlank(first.clozeText().trim());
        sentence.setTrueAnswerSurface(first.answerText().trim());
        sentence.setNormalizedTrueAnswer(normalize(first.answerText()));
        sentence.setHint(first.hint() == null ? "" : first.hint().trim());
        sentence.setSource("LLM");
        return sentencePoolRepo.save(sentence);
    }

    private void markShown(String userId, String sentenceId) {
        var existing = usageRepo.findByUserIdAndSentenceId(userId, sentenceId).orElse(null);
        var now = Instant.now();
        if (existing != null) {
            existing.setLastSeenAt(now);
            existing.setTimesShown(existing.getTimesShown() + 1);
            usageRepo.save(existing);
            return;
        }
        var usage = new StudyUserSentenceUsageEntity();
        usage.setId(UUID.randomUUID().toString());
        usage.setUserId(userId);
        usage.setSentenceId(sentenceId);
        usage.setFirstSeenAt(now);
        usage.setLastSeenAt(now);
        usage.setTimesShown(1);
        usage.setTimesCorrect(0);
        usage.setTimesWrong(0);
        usageRepo.save(usage);
    }

    private Rating mapRating(double semantic, double form, double confidence) {
        if (semantic >= 0.9 && form >= 0.85 && confidence >= 0.4) {
            return Rating.GOOD;
        }
        if (semantic >= 0.55) {
            return Rating.HARD;
        }
        return Rating.AGAIN;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private int dueBucket(VocabularyFlashcardReviewRecord review, Instant now) {
        if (review == null || review.due() == null) return 2;
        return review.due().isAfter(now) ? 1 : 0;
    }

    private Duration overdueDurationOrZero(VocabularyFlashcardReviewRecord review, Instant now) {
        if (review == null || review.due() == null || review.due().isAfter(now)) {
            return Duration.ZERO;
        }
        return Duration.between(review.due(), now);
    }

    private double retrievabilityOrMax(VocabularyFlashcardReviewRecord review) {
        if (review == null || Double.isNaN(review.retrievability())) {
            return Double.MAX_VALUE;
        }
        return review.retrievability();
    }

    private Duration timeUntilDueOrMax(VocabularyFlashcardReviewRecord review, Instant now) {
        if (review == null || review.due() == null) {
            return Duration.ofDays(36500);
        }
        return Duration.between(now, review.due()).abs();
    }

    private int statePriority(State state) {
        if (state == null) return 4;
        return switch (state) {
            case RE_LEARNING -> 0;
            case LEARNING -> 1;
            case REVIEW -> 2;
            case NEW -> 3;
        };
    }

    private record StudyCandidate(VocabularyFlashcardReviewRecord review, Vocabulary vocabulary) {}
}
