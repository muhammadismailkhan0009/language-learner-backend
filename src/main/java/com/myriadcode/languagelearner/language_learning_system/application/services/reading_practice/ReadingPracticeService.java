package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.reading_practice.ReadingPracticeApiMapper;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticePolicy;
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
public class ReadingPracticeService {

    private static final String DIFFICULTY_LEVEL = "B1";
    private static final int RECENT_TOPIC_LIMIT = 10;
    private static final ReadingPracticeApiMapper READING_PRACTICE_API_MAPPER = ReadingPracticeApiMapper.INSTANCE;

    private final ReadingPracticeRepo readingPracticeRepo;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;
    private final ReadingPracticeLlmApi readingPracticeLlmApi;

    private final ReadingPracticePolicy readingPracticePolicy = new ReadingPracticePolicy();

    public ReadingPracticeService(ReadingPracticeRepo readingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  ReadingPracticeLlmApi readingPracticeLlmApi) {
        this.readingPracticeRepo = readingPracticeRepo;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.fetchPrivateVocabularyApi = fetchPrivateVocabularyApi;
        this.readingPracticeLlmApi = readingPracticeLlmApi;
    }

    public void createSession(String userId) {
        var flashcards = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        if (flashcards.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary flashcards found for user");
        }

        var vocabRecords = fetchVocabularyRecords(userId, flashcards);
        var candidates = buildCandidates(flashcards, vocabRecords);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary candidates found for reading practice");
        }

        var rotationHour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        var selected = readingPracticePolicy.selectCandidates(userId, candidates, rotationHour);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary for reading practice");
        }

        var selectedVocab = selected.stream()
                .map(candidate -> vocabRecords.get(candidate.vocabularyId()))
                .filter(record -> record != null)
                .map(record -> new ReadingPracticeVocabularySeed(record.surface(), record.translation()))
                .toList();

        var previousTopics = readingPracticeRepo.findRecentTopicsByUserId(userId, RECENT_TOPIC_LIMIT);
        var topic = readingPracticeLlmApi.selectTopicForTextGeneration(selectedVocab, previousTopics, DIFFICULTY_LEVEL);
        if (topic == null || topic.isBlank()) {
            topic = "General practice";
        }

        var generated = readingPracticeLlmApi.generateReadingContent(topic, selectedVocab, DIFFICULTY_LEVEL);
        var paragraphs = buildParagraphs(generated);
        var readingText = joinParagraphs(paragraphs);
        var usedVocabularySurfaces = findUsedVocabularySurfaces(selectedVocab, readingText);

        var usageRecords = buildUsageRecords(selected, vocabRecords, usedVocabularySurfaces);

        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId(UUID.randomUUID().toString()),
                new UserId(userId),
                topic,
                readingText,
                paragraphs,
                Instant.now(),
                usageRecords
        );

        readingPracticeRepo.save(session);
    }

    public ReadingPracticeSessionResponse getSession(String userId, String sessionId) {
        var session = readingPracticeRepo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Reading session not found"));
        return toSessionResponse(session, userId);
    }

    public List<ReadingPracticeSessionSummaryResponse> listSessions(String userId) {
        return readingPracticeRepo.findAllByUserId(userId).stream()
                .map(READING_PRACTICE_API_MAPPER::toSummary)
                .toList();
    }

    public void deleteSession(String userId, String sessionId) {
        readingPracticeRepo.deleteByIdAndUserId(sessionId, userId);
    }

    public void detachFlashcard(String userId, String sessionId, String flashcardId) {
        readingPracticeRepo.detachFlashcard(userId, sessionId, flashcardId);
    }

    private List<ReadingVocabularyFlashCardView> buildFlashcards(String userId,
                                                                 List<ReadingVocabularyUsage> usages) {
        if (usages == null || usages.isEmpty()) {
            return List.of();
        }
        var vocabIds = usages.stream().map(ReadingVocabularyUsage::vocabularyId).distinct().toList();
        var vocabRecords = fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));

        return usages.stream()
                .map(usage -> toFlashcardView(usage, vocabRecords.get(usage.vocabularyId())))
                .filter(view -> view != null)
                .toList();
    }

    private Map<String, PrivateVocabularyRecord> fetchVocabularyRecords(
            String userId,
            List<VocabularyFlashcardReviewRecord> flashcards
    ) {
        var vocabIds = flashcards.stream().map(VocabularyFlashcardReviewRecord::vocabularyId).distinct().toList();
        return fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));
    }

    private ReadingPracticeSessionResponse toSessionResponse(ReadingPracticeSession session, String userId) {
        var response = READING_PRACTICE_API_MAPPER.toResponse(session);
        var flashcards = buildFlashcards(userId, session.vocabularyUsages());
        List<com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeParagraphResponse> paragraphs =
                response.readingParagraphs() == null ? List.of() : response.readingParagraphs();
        return new ReadingPracticeSessionResponse(
                response.sessionId(),
                response.topic(),
                response.readingText(),
                paragraphs,
                flashcards,
                response.createdAt()
        );
    }

    private List<ReadingVocabularyUsage> buildUsageRecords(List<ReadingPracticeCandidate> selected,
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
                        ReadingPracticeCandidate::flashCardId,
                        candidate -> new ReadingVocabularyUsage(
                                new ReadingVocabularyUsage.ReadingVocabularyUsageId(UUID.randomUUID().toString()),
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

    private List<ReadingPracticeCandidate> buildCandidates(
            List<VocabularyFlashcardReviewRecord> flashcards,
            Map<String, PrivateVocabularyRecord> vocabRecords
    ) {
        return flashcards.stream()
                .filter(review -> !review.isReversed())
                .map(review -> {
                    var vocab = vocabRecords.get(review.vocabularyId());
                    if (vocab == null) {
                        return null;
                    }
                    var createdAt = vocab.createdAt() == null ? Instant.EPOCH : vocab.createdAt();
                    return new ReadingPracticeCandidate(
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
                .filter(candidate -> candidate != null)
                .toList();
    }

    private ReadingVocabularyFlashCardView toFlashcardView(ReadingVocabularyUsage usage,
                                                           PrivateVocabularyRecord record) {
        if (record == null) {
            return null;
        }
        var front = record.surface();
        var back = record.translation();
        var sentences = record.exampleSentences().stream()
                .map(sentence -> new ReadingVocabularyFlashCardView.Sentence(
                        sentence.id(),
                        sentence.sentence(),
                        sentence.translation()
                ))
                .toList();
        return new ReadingVocabularyFlashCardView(
                usage.flashCardId(),
                new ReadingVocabularyFlashCardView.Front(front),
                new ReadingVocabularyFlashCardView.Back(back, sentences),
                false
        );
    }

    private Set<String> findUsedVocabularySurfaces(List<ReadingPracticeVocabularySeed> selectedVocab,
                                                   String readingText) {
        return readingPracticeLlmApi.identifyUsedVocabulary(selectedVocab, readingText).stream()
                .map(this::normalizeSurface)
                .filter(surface -> !surface.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<ReadingPracticeParagraph> buildParagraphs(ReadingPracticeReadingContent generated) {
        if (generated == null || generated.paragraphs() == null) {
            return List.of();
        }
        return java.util.stream.IntStream.range(0, generated.paragraphs().size())
                .mapToObj(index -> {
                    var paragraph = generated.paragraphs().get(index);
                    var sentences = paragraph.sentences() == null ? List.<ReadingPracticeSentence>of()
                            : java.util.stream.IntStream.range(0, paragraph.sentences().size())
                            .mapToObj(sentenceIndex -> new ReadingPracticeSentence(
                                    new ReadingPracticeSentence.ReadingPracticeSentenceId(UUID.randomUUID().toString()),
                                    paragraph.sentences().get(sentenceIndex),
                                    sentenceIndex
                            ))
                            .toList();
                    return new ReadingPracticeParagraph(
                            new ReadingPracticeParagraph.ReadingPracticeParagraphId(UUID.randomUUID().toString()),
                            paragraph.text(),
                            index,
                            sentences
                    );
                })
                .toList();
    }

    private String joinParagraphs(List<ReadingPracticeParagraph> paragraphs) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return "";
        }
        return paragraphs.stream()
                .map(ReadingPracticeParagraph::text)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

    private String normalizeSurface(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
