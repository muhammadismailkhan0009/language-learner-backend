package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingSubmissionFeedbackLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.writing_practice.WritingPracticeApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticePolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WritingPracticeService {

    private static final String DIFFICULTY_LEVEL = "B1";
    private static final int RECENT_TOPIC_LIMIT = 10;
    private static final WritingPracticeApiMapper WRITING_PRACTICE_API_MAPPER = WritingPracticeApiMapper.INSTANCE;

    private final WritingPracticeRepo writingPracticeRepo;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;
    private final WritingPracticeLlmApi writingPracticeLlmApi;
    private final PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo;
    private final WritingSubmissionFeedbackLlmApi writingSubmissionFeedbackLlmApi;
    private final WritingPracticePolicy writingPracticePolicy = new WritingPracticePolicy();
    private final WritingPracticeCandidateAssembler candidateAssembler = new WritingPracticeCandidateAssembler();
    private final WritingPracticeContentAssembler contentAssembler = new WritingPracticeContentAssembler();

    public WritingPracticeService(WritingPracticeRepo writingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  WritingPracticeLlmApi writingPracticeLlmApi,
                                  PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo,
                                  WritingSubmissionFeedbackLlmApi writingSubmissionFeedbackLlmApi) {
        this.writingPracticeRepo = writingPracticeRepo;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.fetchPrivateVocabularyApi = fetchPrivateVocabularyApi;
        this.writingPracticeLlmApi = writingPracticeLlmApi;
        this.practiceVocabularyReferenceRepo = practiceVocabularyReferenceRepo;
        this.writingSubmissionFeedbackLlmApi = writingSubmissionFeedbackLlmApi;
    }

    public void createSessionReactive(String userId) {
        var normalizedUserId = requireUserId(userId);
        Vals.runIo(() -> {
            try {
                createSession(normalizedUserId);
            } catch (Exception exception) {
                log.error("Writing session background creation failed for userId={}", normalizedUserId, exception);
            }
        });
    }

    public void createSession(String userId) {
        var normalizedUserId = requireUserId(userId);
        var flashcards = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(normalizedUserId);
        if (flashcards.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary flashcards found for user");
        }
        var practiceReferences = practiceVocabularyReferenceRepo.findByUserId(normalizedUserId);
        if (practiceReferences.isEmpty()) {
            throw new IllegalArgumentException("No practice vocabulary references found for user");
        }
        var practiceVocabularyIds = practiceReferences.stream()
                .map(reference -> reference.vocabularyId().id())
                .collect(Collectors.toSet());
        var practiceFlashcards = candidateAssembler.filterByPracticeVocabulary(flashcards, practiceVocabularyIds);
        if (practiceFlashcards.isEmpty()) {
            throw new IllegalArgumentException("No flashcards found for practice vocabulary references");
        }

        var vocabRecords = fetchVocabularyRecords(normalizedUserId, practiceFlashcards);
        var candidates = candidateAssembler.buildCandidates(practiceFlashcards, vocabRecords);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary candidates found for writing practice");
        }

        var rotationHour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        var selected = writingPracticePolicy.selectCandidates(normalizedUserId, candidates, rotationHour);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary for writing practice");
        }

        var selectedVocab = candidateAssembler.toVocabularySeeds(selected, vocabRecords);
        if (selectedVocab.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary seeds found for writing practice");
        }

        var previousTopics = writingPracticeRepo.findRecentTopicsByUserId(normalizedUserId, RECENT_TOPIC_LIMIT);
        String topic;
        String englishParagraph;
        String germanParagraph;
        Set<String> usedVocabularySurfaces;
        List<WritingSentencePair> sentencePairs;
        try (var ignored = LlmUserContextHolder.scoped(normalizedUserId)) {
            topic = writingPracticeLlmApi.selectTopicForWriting(selectedVocab, previousTopics, DIFFICULTY_LEVEL);
            if (topic == null || topic.isBlank()) {
                topic = "General writing practice";
            }

            var bilingualContent = writingPracticeLlmApi.generateBilingualContent(topic, selectedVocab, DIFFICULTY_LEVEL);
            englishParagraph = contentAssembler.sanitizeParagraph(bilingualContent.englishParagraph());
            germanParagraph = contentAssembler.sanitizeParagraph(bilingualContent.germanParagraph());
            if (englishParagraph.isBlank() || germanParagraph.isBlank()) {
                throw new IllegalArgumentException("Unable to generate writing content");
            }
            usedVocabularySurfaces = contentAssembler.findUsedVocabularySurfaces(
                    writingPracticeLlmApi,
                    selectedVocab,
                    englishParagraph,
                    germanParagraph
            );
            sentencePairs = contentAssembler.buildSentencePairs(
                    writingPracticeLlmApi.splitIntoSentencePairs(englishParagraph, germanParagraph),
                    englishParagraph,
                    germanParagraph
            );
            if (sentencePairs.isEmpty()) {
                throw new IllegalArgumentException("Unable to generate writing sentence pairs");
            }
        }

        var usages = candidateAssembler.buildUsages(selected, vocabRecords, usedVocabularySurfaces);

        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId(UUID.randomUUID().toString()),
                new UserId(normalizedUserId),
                topic,
                englishParagraph,
                germanParagraph,
                Instant.now(),
                null,
                null,
                null,
                null,
                sentencePairs,
                usages
        );

        writingPracticeRepo.save(session);
    }

    public WritingPracticeSessionResponse getSession(String userId, String sessionId) {
        var normalizedUserId = requireUserId(userId);
        var session = writingPracticeRepo.findByIdAndUserId(sessionId, normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        return toSessionResponse(session, normalizedUserId);
    }

    public List<WritingPracticeSessionSummaryResponse> listSessions(String userId) {
        var normalizedUserId = requireUserId(userId);
        return writingPracticeRepo.findAllByUserId(normalizedUserId).stream()
                .map(WRITING_PRACTICE_API_MAPPER::toSummary)
                .toList();
    }

    public void deleteSession(String userId, String sessionId) {
        var normalizedUserId = requireUserId(userId);
        writingPracticeRepo.deleteByIdAndUserId(sessionId, normalizedUserId);
    }

    public void detachFlashcard(String userId, String sessionId, String flashcardId) {
        var normalizedUserId = requireUserId(userId);
        writingPracticeRepo.detachFlashcard(normalizedUserId, sessionId, flashcardId);
    }

    public void submitAnswer(String userId, String sessionId, String submittedAnswer) {
        var normalizedUserId = requireUserId(userId);
        var sanitizedAnswer = sanitizeSubmission(submittedAnswer);
        if (sanitizedAnswer.isBlank()) {
            throw new IllegalArgumentException("Submitted answer must not be blank");
        }
        var session = writingPracticeRepo.findByIdAndUserId(sessionId, normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));

        String feedback;
        try (var ignored = LlmUserContextHolder.scoped(normalizedUserId)) {
            feedback = writingSubmissionFeedbackLlmApi.generateFeedback(
                    session.englishParagraph(),
                    session.germanParagraph(),
                    sanitizedAnswer
            );
        }
        var submittedAt = Instant.now();
        writingPracticeRepo.updateSubmission(sessionId, normalizedUserId, sanitizedAnswer, submittedAt, feedback, submittedAt);
    }

    private WritingPracticeSessionResponse toSessionResponse(WritingPracticeSession session, String userId) {
        var flashcards = buildFlashcards(userId, session.vocabularyUsages());
        return WRITING_PRACTICE_API_MAPPER.toResponse(session, flashcards);
    }

    private List<WritingVocabularyFlashCardView> buildFlashcards(String userId,
                                                                 List<WritingVocabularyUsage> usages) {
        if (usages == null || usages.isEmpty()) {
            return List.of();
        }
        var vocabIds = usages.stream().map(WritingVocabularyUsage::vocabularyId).distinct().toList();
        var vocabRecords = fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));

        return usages.stream()
                .map(usage -> toFlashcardView(usage, vocabRecords.get(usage.vocabularyId())))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private WritingVocabularyFlashCardView toFlashcardView(WritingVocabularyUsage usage,
                                                           PrivateVocabularyRecord record) {
        if (record == null) {
            return null;
        }
        var sentences = record.exampleSentences().stream()
                .map(sentence -> new WritingVocabularyFlashCardView.Sentence(
                        sentence.id(),
                        sentence.sentence(),
                        sentence.translation()
                ))
                .toList();
        return new WritingVocabularyFlashCardView(
                usage.flashCardId(),
                new WritingVocabularyFlashCardView.Front(record.translation()),
                new WritingVocabularyFlashCardView.Back(record.surface(), sentences),
                true
        );
    }

    private Map<String, PrivateVocabularyRecord> fetchVocabularyRecords(String userId,
                                                                        List<VocabularyFlashcardReviewRecord> flashcards) {
        var vocabIds = flashcards.stream().map(VocabularyFlashcardReviewRecord::vocabularyId).distinct().toList();
        return fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));
    }

    private String sanitizeSubmission(String value) {
        return value == null ? "" : value.trim();
    }

    private String requireUserId(String userId) {
        var normalized = sanitizeSubmission(userId);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return normalized;
    }
}
