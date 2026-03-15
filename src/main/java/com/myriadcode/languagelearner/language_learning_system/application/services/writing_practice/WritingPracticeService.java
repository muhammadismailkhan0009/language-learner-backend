package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
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
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticePolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WritingPracticeService {

    private static final String DIFFICULTY_LEVEL = "B1";
    private static final int RECENT_TOPIC_LIMIT = 10;
    private static final WritingPracticeApiMapper WRITING_PRACTICE_API_MAPPER = WritingPracticeApiMapper.INSTANCE;

    private final WritingPracticeRepo writingPracticeRepo;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;
    private final WritingPracticeLlmApi writingPracticeLlmApi;
    private final WritingPracticePolicy writingPracticePolicy = new WritingPracticePolicy();

    public WritingPracticeService(WritingPracticeRepo writingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  WritingPracticeLlmApi writingPracticeLlmApi) {
        this.writingPracticeRepo = writingPracticeRepo;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.fetchPrivateVocabularyApi = fetchPrivateVocabularyApi;
        this.writingPracticeLlmApi = writingPracticeLlmApi;
    }

    public void createSession(String userId) {
        var flashcards = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        if (flashcards.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary flashcards found for user");
        }

        var vocabRecords = fetchVocabularyRecords(userId, flashcards);
        var candidates = buildCandidates(flashcards, vocabRecords);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary candidates found for writing practice");
        }

        var rotationHour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        var selected = writingPracticePolicy.selectCandidates(userId, candidates, rotationHour);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary for writing practice");
        }

        var selectedVocab = selected.stream()
                .map(candidate -> vocabRecords.get(candidate.vocabularyId()))
                .filter(record -> record != null)
                .map(record -> new WritingPracticeVocabularySeed(record.surface(), record.translation()))
                .toList();

        var previousTopics = writingPracticeRepo.findRecentTopicsByUserId(userId, RECENT_TOPIC_LIMIT);
        var topic = writingPracticeLlmApi.selectTopicForWriting(selectedVocab, previousTopics, DIFFICULTY_LEVEL);
        if (topic == null || topic.isBlank()) {
            topic = "General writing practice";
        }

        var bilingualContent = writingPracticeLlmApi.generateBilingualContent(topic, selectedVocab, DIFFICULTY_LEVEL);
        var englishParagraph = sanitizeParagraph(bilingualContent.englishParagraph());
        var germanParagraph = sanitizeParagraph(bilingualContent.germanParagraph());
        var usedVocabularySurfaces = findUsedVocabularySurfaces(selectedVocab, englishParagraph, germanParagraph);
        var sentencePairs = buildSentencePairs(
                writingPracticeLlmApi.splitIntoSentencePairs(englishParagraph, germanParagraph),
                englishParagraph,
                germanParagraph
        );

        var usages = buildUsages(selected, vocabRecords, usedVocabularySurfaces);

        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId(UUID.randomUUID().toString()),
                new UserId(userId),
                topic,
                englishParagraph,
                germanParagraph,
                Instant.now(),
                null,
                null,
                sentencePairs,
                usages
        );

        writingPracticeRepo.save(session);
    }

    public WritingPracticeSessionResponse getSession(String userId, String sessionId) {
        var session = writingPracticeRepo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        return toSessionResponse(session, userId);
    }

    public List<WritingPracticeSessionSummaryResponse> listSessions(String userId) {
        return writingPracticeRepo.findAllByUserId(userId).stream()
                .map(WRITING_PRACTICE_API_MAPPER::toSummary)
                .toList();
    }

    public void deleteSession(String userId, String sessionId) {
        writingPracticeRepo.deleteByIdAndUserId(sessionId, userId);
    }

    public void detachFlashcard(String userId, String sessionId, String flashcardId) {
        writingPracticeRepo.detachFlashcard(userId, sessionId, flashcardId);
    }

    public void submitAnswer(String userId, String sessionId, String submittedAnswer) {
        var sanitizedAnswer = sanitizeSubmission(submittedAnswer);
        if (sanitizedAnswer.isBlank()) {
            throw new IllegalArgumentException("Submitted answer must not be blank");
        }
        writingPracticeRepo.updateSubmission(sessionId, userId, sanitizedAnswer, Instant.now());
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

    private List<WritingVocabularyUsage> buildUsages(List<WritingPracticeCandidate> selected,
                                                     Map<String, PrivateVocabularyRecord> vocabRecords,
                                                     Set<String> usedVocabularySurfaces) {
        if (selected == null || selected.isEmpty() || usedVocabularySurfaces.isEmpty()) {
            return List.of();
        }
        return selected.stream()
                .filter(candidate -> {
                    var vocab = vocabRecords.get(candidate.vocabularyId());
                    return vocab != null && usedVocabularySurfaces.contains(normalizeSurface(vocab.surface()));
                })
                .collect(Collectors.toMap(
                        WritingPracticeCandidate::flashCardId,
                        candidate -> new WritingVocabularyUsage(
                                new WritingVocabularyUsage.WritingVocabularyUsageId(UUID.randomUUID().toString()),
                                candidate.flashCardId(),
                                candidate.vocabularyId()
                        ),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private Map<String, PrivateVocabularyRecord> fetchVocabularyRecords(String userId,
                                                                        List<VocabularyFlashcardReviewRecord> flashcards) {
        var vocabIds = flashcards.stream().map(VocabularyFlashcardReviewRecord::vocabularyId).distinct().toList();
        return fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));
    }

    private List<WritingPracticeCandidate> buildCandidates(List<VocabularyFlashcardReviewRecord> flashcards,
                                                           Map<String, PrivateVocabularyRecord> vocabRecords) {
        return flashcards.stream()
                .filter(VocabularyFlashcardReviewRecord::isReversed)
                .map(review -> {
                    var vocab = vocabRecords.get(review.vocabularyId());
                    if (vocab == null) {
                        return null;
                    }
                    var createdAt = vocab.createdAt() == null ? Instant.EPOCH : vocab.createdAt();
                    return new WritingPracticeCandidate(
                            review.flashcardId(),
                            review.vocabularyId(),
                            review.fsrsState(),
                            createdAt,
                            review.due(),
                            review.retrievability(),
                            review.lapses(),
                            review.lastReview()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private Set<String> findUsedVocabularySurfaces(List<WritingPracticeVocabularySeed> selectedVocab,
                                                   String englishParagraph,
                                                   String germanParagraph) {
        return writingPracticeLlmApi.identifyUsedVocabulary(selectedVocab, englishParagraph, germanParagraph).stream()
                .map(this::normalizeSurface)
                .filter(surface -> !surface.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<WritingSentencePair> buildSentencePairs(List<WritingPracticeSentencePairSeed> pairs,
                                                         String englishParagraph,
                                                         String germanParagraph) {
        if (pairs == null || pairs.isEmpty()) {
            return List.of(new WritingSentencePair(
                    new WritingSentencePair.WritingSentencePairId(UUID.randomUUID().toString()),
                    englishParagraph,
                    germanParagraph,
                    0
            ));
        }
        return java.util.stream.IntStream.range(0, pairs.size())
                .mapToObj(index -> {
                    var pair = pairs.get(index);
                    return new WritingSentencePair(
                            new WritingSentencePair.WritingSentencePairId(UUID.randomUUID().toString()),
                            sanitizeParagraph(pair.englishSentence()),
                            sanitizeParagraph(pair.germanSentence()),
                            index
                    );
                })
                .toList();
    }

    private String sanitizeParagraph(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String sanitizeSubmission(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeSurface(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
