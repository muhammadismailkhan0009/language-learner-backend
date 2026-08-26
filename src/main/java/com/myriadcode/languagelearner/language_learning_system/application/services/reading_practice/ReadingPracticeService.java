package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.services.reading_practice.ReadingPracticeReadingContentValidator;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeScenarioResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.reading_practice.ReadingPracticeApiMapper;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services.ReadingPracticePolicy;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
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

    private static final int RECENT_TOPIC_LIMIT = 10;
    private static final ReadingPracticeApiMapper READING_PRACTICE_API_MAPPER = ReadingPracticeApiMapper.INSTANCE;

    private final ReadingPracticeRepo readingPracticeRepo;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;
    private final ReadingPracticeLlmApi readingPracticeLlmApi;
    private final ReadingGenerationContextService readingGenerationContextService;
    private final RecentExerciseVocabularyUsageService recentExerciseVocabularyUsageService;
    private final ReadingPracticeReadingContentValidator contentValidator = new ReadingPracticeReadingContentValidator();

    private final ReadingPracticePolicy readingPracticePolicy = new ReadingPracticePolicy();

    public ReadingPracticeService(ReadingPracticeRepo readingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  ReadingPracticeLlmApi readingPracticeLlmApi,
                                  ReadingGenerationContextService readingGenerationContextService) {
        this(readingPracticeRepo, vocabularyFlashcardReviewsApi, fetchPrivateVocabularyApi,
                readingPracticeLlmApi, readingGenerationContextService, null);
    }

    @Autowired
    public ReadingPracticeService(ReadingPracticeRepo readingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  ReadingPracticeLlmApi readingPracticeLlmApi,
                                  ReadingGenerationContextService readingGenerationContextService,
                                  RecentExerciseVocabularyUsageService recentExerciseVocabularyUsageService) {
        this.readingPracticeRepo = readingPracticeRepo;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.fetchPrivateVocabularyApi = fetchPrivateVocabularyApi;
        this.readingPracticeLlmApi = readingPracticeLlmApi;
        this.readingGenerationContextService = readingGenerationContextService;
        this.recentExerciseVocabularyUsageService = recentExerciseVocabularyUsageService;
    }

    public void createSession(String userId) {
        var preparation = prepareGeneration(userId);
        ReadingPracticeReadingContent generated;
        try (var ignored = LlmUserContextHolder.scoped(userId)) {
            generated = readingPracticeLlmApi.generateReadingContent(
                    preparation.sources(), preparation.previousTopics(),
                    preparation.generationContext().learnerLevel(),
                    preparation.generationContext().grammarRuleTitles(), 3
            );
        }
        storePreparedGeneration(userId, preparation, generated);
    }

    public String prepareGenerationPrompt(String userId) {
        var preparation = prepareGeneration(userId);
        return readingPracticeLlmApi.buildReadingContentPrompt(
                preparation.sources(), preparation.previousTopics(),
                preparation.generationContext().learnerLevel(),
                preparation.generationContext().grammarRuleTitles(), 3
        );
    }

    public void storeGeneration(String userId, ReadingPracticeReadingContent generated) {
        storePreparedGeneration(userId, prepareGeneration(userId), generated);
    }

    private ReadingPracticeGenerationPreparation prepareGeneration(String userId) {
        var flashcards = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        if (flashcards.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary flashcards found for user");
        }

        var vocabRecords = fetchVocabularyRecords(userId, flashcards);
        var candidates = buildCandidates(flashcards, vocabRecords);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary candidates found for reading practice");
        }

        var recentUsageCounts = recentExerciseVocabularyUsageService == null
                ? Map.<String, Integer>of()
                : recentExerciseVocabularyUsageService.countRecentSessionUsage(userId);
        var selected = readingPracticePolicy.selectCandidates(candidates, Instant.now(), recentUsageCounts);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary for reading practice");
        }

        var previousTopics = readingPracticeRepo.findRecentTopicsByUserId(userId, RECENT_TOPIC_LIMIT);
        var generationContext = readingGenerationContextService.build(userId);
        var sources = selected.stream().map(candidate -> {
            var record = vocabRecords.get(candidate.vocabularyId());
            return record == null ? null : new ReadingPracticeVocabularySeed(
                    candidate.vocabularyId(), record.surface(), record.translation());
        }).filter(java.util.Objects::nonNull).toList();
        return new ReadingPracticeGenerationPreparation(selected, vocabRecords, previousTopics, generationContext, sources);
    }

    private void storePreparedGeneration(String userId, ReadingPracticeGenerationPreparation preparation,
                                         ReadingPracticeReadingContent generated) {
        var errors = contentValidator.validate(3, preparation.sources(), generated);
        if (!errors.isEmpty()) {
            throw new ReadingPracticeValidationException(errors);
        }
        var candidatesByVocabularyId = preparation.selected().stream().collect(Collectors.toMap(
                ReadingPracticeCandidate::vocabularyId, Function.identity(), (first, ignored) -> first));
        var candidatesBySurface = preparation.sources().stream().collect(Collectors.toMap(
                seed -> normalizeSurface(seed.surface()),
                seed -> candidatesByVocabularyId.get(seed.id()),
                (first, ignored) -> first));
        var scenarios = java.util.stream.IntStream.range(0, generated.scenarios().size())
                .mapToObj(index -> buildScenario(generated.scenarios().get(index), index,
                        candidatesByVocabularyId, candidatesBySurface))
                .toList();
        var first = scenarios.getFirst();
        readingPracticeRepo.save(new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId(UUID.randomUUID().toString()),
                new UserId(userId), first.label(), first.readingText(), first.paragraphs(), Instant.now(),
                first.vocabularyUsages(), scenarios));
    }

    private record ReadingPracticeGenerationPreparation(
            List<ReadingPracticeCandidate> selected,
            Map<String, PrivateVocabularyRecord> vocabularyRecords,
            List<String> previousTopics,
            ReadingGenerationContext generationContext,
            List<ReadingPracticeVocabularySeed> sources
    ) {
    }

    private ReadingPracticeScenario buildScenario(ReadingPracticeReadingContent.Scenario generated, int position,
                                                  Map<String, ReadingPracticeCandidate> candidatesByVocabularyId,
                                                  Map<String, ReadingPracticeCandidate> candidatesBySurface) {
        var paragraphs = java.util.stream.IntStream.range(0, generated.paragraphs().size()).mapToObj(index -> {
            var source = generated.paragraphs().get(index);
            var sentences = java.util.stream.IntStream.range(0, source.sentences().size())
                    .mapToObj(sentenceIndex -> new ReadingPracticeSentence(
                            new ReadingPracticeSentence.ReadingPracticeSentenceId(UUID.randomUUID().toString()),
                            source.sentences().get(sentenceIndex), sentenceIndex)).toList();
            return new ReadingPracticeParagraph(
                    new ReadingPracticeParagraph.ReadingPracticeParagraphId(UUID.randomUUID().toString()),
                    source.text(), index, sentences);
        }).toList();
        var usages = generated.usedVocabulary().stream()
                .map(reference -> {
                    var candidate = candidatesByVocabularyId.get(reference.vocabularyId());
                    return candidate != null ? candidate : candidatesBySurface.get(normalizeSurface(reference.surface()));
                }).filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(ReadingPracticeCandidate::vocabularyId, Function.identity(),
                        (firstCandidate, ignored) -> firstCandidate, LinkedHashMap::new)).values().stream()
                .map(candidate -> new ReadingVocabularyUsage(
                        new ReadingVocabularyUsage.ReadingVocabularyUsageId(UUID.randomUUID().toString()),
                        candidate.flashCardId(), candidate.vocabularyId())).toList();
        return new ReadingPracticeScenario(
                new ReadingPracticeScenario.ReadingPracticeScenarioId(UUID.randomUUID().toString()),
                generated.scenarioLabel(), joinParagraphs(paragraphs), position, paragraphs, usages);
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
        var scenarios = session.scenarios() == null ? List.<ReadingPracticeScenarioResponse>of()
                : session.scenarios().stream().map(scenario -> new ReadingPracticeScenarioResponse(
                        scenario.id().id(), scenario.label(), scenario.readingText(),
                        scenario.paragraphs().stream().map(READING_PRACTICE_API_MAPPER::toParagraphResponse).toList(),
                        buildFlashcards(userId, scenario.vocabularyUsages()))).toList();
        return new ReadingPracticeSessionResponse(
                response.sessionId(),
                response.topic(),
                response.readingText(),
                paragraphs,
                flashcards,
                response.createdAt(),
                scenarios
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
                .filter(VocabularyFlashcardReviewRecord::isReversed)
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
                true
        );
    }

    private Set<String> findUsedVocabularySurfaces(List<ReadingPracticeVocabularySeed> selectedVocab,
                                                   String readingText) {
        return readingPracticeLlmApi.identifyUsedVocabulary(selectedVocab, readingText).stream()
                .map(this::normalizeSurface)
                .filter(surface -> !surface.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
