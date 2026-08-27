package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeGeneration;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
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
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticePolicy;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WritingPracticeService {

    private static final String FEEDBACK_DIFFICULTY_LEVEL = "B1";
    private static final int RECENT_TOPIC_LIMIT = 10;
    private static final int SCENARIO_COUNT = 3;
    private static final WritingPracticeApiMapper WRITING_PRACTICE_API_MAPPER = WritingPracticeApiMapper.INSTANCE;

    private final WritingPracticeRepo writingPracticeRepo;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;
    private final PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo;
    private final WritingFeedbackPipelineService writingFeedbackPipelineService;
    private final WritingGenerationContextService writingGenerationContextService;
    private final UserDifficultyLevelApi userDifficultyLevelApi;
    private final RecentExerciseVocabularyUsageService recentExerciseVocabularyUsageService;
    private final WritingPracticePolicy writingPracticePolicy = new WritingPracticePolicy();
    private final WritingPracticeCandidateAssembler candidateAssembler = new WritingPracticeCandidateAssembler();
    private final WritingPracticeContentAssembler contentAssembler = new WritingPracticeContentAssembler();

    @Autowired
    public WritingPracticeService(WritingPracticeRepo writingPracticeRepo,
                                  FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                  FetchPrivateVocabularyApi fetchPrivateVocabularyApi,
                                  PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo,
                                  WritingFeedbackPipelineService writingFeedbackPipelineService,
                                  WritingGenerationContextService writingGenerationContextService,
                                  UserDifficultyLevelApi userDifficultyLevelApi,
                                  RecentExerciseVocabularyUsageService recentExerciseVocabularyUsageService) {
        this.writingPracticeRepo = writingPracticeRepo;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.fetchPrivateVocabularyApi = fetchPrivateVocabularyApi;
        this.practiceVocabularyReferenceRepo = practiceVocabularyReferenceRepo;
        this.writingFeedbackPipelineService = writingFeedbackPipelineService;
        this.writingGenerationContextService = writingGenerationContextService;
        this.userDifficultyLevelApi = userDifficultyLevelApi;
        this.recentExerciseVocabularyUsageService = recentExerciseVocabularyUsageService;
    }

    public String prepareGenerationPrompt(String userId) {
        var preparation = prepareGeneration(userId);
        return PromptsGenerator.writingPracticeGeneration(preparation.selectedVocabulary(), preparation.previousTopics(),
                preparation.generationContext().learnerLevel(), preparation.generationContext().grammarRuleTitles(), SCENARIO_COUNT);
    }

    public void storeGeneration(String userId, WritingPracticeGeneration generated) {
        var normalizedUserId = requireUserId(userId);
        var preparation = prepareGeneration(normalizedUserId);
        var errors = validateGeneration(generated);
        if (!errors.isEmpty()) throw new WritingPracticeGenerationValidationException(errors);

        List<WritingPracticeScenario> scenarios;
        try {
            scenarios = java.util.stream.IntStream.range(0, generated.scenarios().size()).mapToObj(position -> {
                var source = generated.scenarios().get(position);
                var english = contentAssembler.sanitizeParagraph(source.englishParagraph());
                var german = contentAssembler.sanitizeParagraph(source.germanParagraph());
                var pairs = contentAssembler.buildSentencePairs(source.sentencePairs(), english, german);
                var usedSurfaces = contentAssembler.findUsedVocabularySurfaces(
                        candidateAssembler.toVocabularySeeds(
                                preparation.candidates(), preparation.vocabularyRecords()),
                        source.usedVocabulary());
                var usages = candidateAssembler.buildUsages(
                        preparation.candidates(), preparation.vocabularyRecords(), usedSurfaces);
                return new WritingPracticeScenario(
                        new WritingPracticeScenario.WritingPracticeScenarioId(UUID.randomUUID().toString()), position,
                        source.topic().trim(), english, german, null, null, null, null, null, pairs, usages);
            }).toList();
        } catch (IllegalArgumentException exception) {
            throw new WritingPracticeGenerationValidationException(List.of(exception.getMessage()));
        }

        writingPracticeRepo.save(new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId(UUID.randomUUID().toString()),
                new UserId(normalizedUserId), Instant.now(), scenarios));
    }

    private WritingPracticeGenerationPreparation prepareGeneration(String userId) {
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

        var recentUsageCounts = recentExerciseVocabularyUsageService == null
                ? Map.<String, Integer>of()
                : recentExerciseVocabularyUsageService.countRecentSessionUsage(normalizedUserId);
        var selected = writingPracticePolicy.selectCandidates(candidates, Instant.now(), recentUsageCounts);
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary for writing practice");
        }

        var selectedVocab = candidateAssembler.toVocabularySeeds(selected, vocabRecords);
        if (selectedVocab.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary seeds found for writing practice");
        }

        var previousTopics = writingPracticeRepo.findRecentTopicsByUserId(normalizedUserId, RECENT_TOPIC_LIMIT);
        var generationContext = writingGenerationContextService == null
                ? new WritingGenerationContext(com.myriadcode.languagelearner.common.enums.LanguageLevel.B1, List.of())
                : writingGenerationContextService.build(normalizedUserId);
        return new WritingPracticeGenerationPreparation(
                candidates, vocabRecords, selectedVocab, previousTopics, generationContext);
    }

    private List<String> validateGeneration(WritingPracticeGeneration generated) {
        var errors = new java.util.ArrayList<String>();
        if (generated == null || generated.scenarios() == null) {
            return List.of("scenarios is required");
        }
        if (generated.scenarios().size() != SCENARIO_COUNT) {
            errors.add("scenarios must contain exactly " + SCENARIO_COUNT + " items");
        }
        for (int index = 0; index < generated.scenarios().size(); index++) {
            var scenario = generated.scenarios().get(index);
            var path = "scenarios[" + index + "]";
            if (scenario == null) { errors.add(path + " is required"); continue; }
            if (scenario.topic() == null || scenario.topic().isBlank()) errors.add(path + ".topic is required");
            if (scenario.englishParagraph() == null || scenario.englishParagraph().isBlank()) errors.add(path + ".englishParagraph is required");
            if (scenario.germanParagraph() == null || scenario.germanParagraph().isBlank()) errors.add(path + ".germanParagraph is required");
            if (scenario.sentencePairs() == null || scenario.sentencePairs().isEmpty()) errors.add(path + ".sentencePairs is required");
            else for (int pairIndex = 0; pairIndex < scenario.sentencePairs().size(); pairIndex++) {
                var pair = scenario.sentencePairs().get(pairIndex);
                if (pair == null || pair.englishSentence() == null || pair.englishSentence().isBlank()
                        || pair.germanSentence() == null || pair.germanSentence().isBlank()) {
                    errors.add(path + ".sentencePairs[" + pairIndex + "] requires both sentences");
                }
            }
            if (scenario.usedVocabulary() == null) errors.add(path + ".usedVocabulary is required");
        }
        return List.copyOf(errors);
    }

    private record WritingPracticeGenerationPreparation(
            List<com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate> candidates,
            Map<String, PrivateVocabularyRecord> vocabularyRecords,
            List<WritingPracticeVocabularySeed> selectedVocabulary,
            List<String> previousTopics,
            WritingGenerationContext generationContext
    ) {}

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

    public void detachFlashcard(String userId, String sessionId, String scenarioId, String flashcardId) {
        var normalizedUserId = requireUserId(userId);
        writingPracticeRepo.detachFlashcard(normalizedUserId, sessionId, scenarioId, flashcardId);
    }

    public void submitAnswer(String userId, String sessionId, String scenarioId, String submittedAnswer, boolean draft) {
        var normalizedUserId = requireUserId(userId);
        var sanitizedAnswer = sanitizeSubmission(submittedAnswer);
        if (sanitizedAnswer.isBlank()) {
            throw new IllegalArgumentException("Submitted answer must not be blank");
        }
        var session = writingPracticeRepo.findByIdAndUserId(sessionId, normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        requireScenario(session, scenarioId);

        if (draft) {
            writingPracticeRepo.updateSubmission(sessionId, scenarioId, normalizedUserId, sanitizedAnswer, null, null, null);
            return;
        }
        var submittedAt = Instant.now();
        writingPracticeRepo.updateSubmission(sessionId, scenarioId, normalizedUserId, sanitizedAnswer, submittedAt, null, null);
    }

    @Transactional
    public WritingPracticeSessionResponse reEvaluateFeedback(String userId, String sessionId, String scenarioId) {
        var normalizedUserId = requireUserId(userId);
        var session = writingPracticeRepo.findByIdAndUserId(sessionId, normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Writing session not found"));
        var scenario = requireScenario(session, scenarioId);
        var submittedAnswer = sanitizeSubmission(scenario.submittedAnswer());
        if (submittedAnswer.isBlank() || scenario.submittedAt() == null) {
            throw new IllegalArgumentException("Writing scenario must have a submitted answer before re-evaluation");
        }
        if (writingFeedbackPipelineService == null) {
            throw new IllegalStateException("Structured writing feedback pipeline is not available");
        }

        WritingFeedbackPipelineService.WritingFeedbackPipelineResult feedback;
        try (var ignored = LlmUserContextHolder.scoped(normalizedUserId)) {
            feedback = writingFeedbackPipelineService.generateFeedback(
                    session.id(), session.userId(), scenario,
                    feedbackLearnerLevel(normalizedUserId),
                    submittedAnswer,
                    buildFeedbackVocabulary(normalizedUserId, scenario.vocabularyUsages()),
                    true
            );
        }

        var generatedAt = Instant.now();
        var updated = writingPracticeRepo.updateSubmission(
                sessionId,
                scenarioId,
                normalizedUserId,
                submittedAnswer,
                scenario.submittedAt(),
                feedback.feedbackText(),
                feedback.structuredFeedback(),
                generatedAt
        );
        return toSessionResponse(updated, normalizedUserId);
    }

    private String feedbackLearnerLevel(String userId) {
        if (userDifficultyLevelApi == null) {
            return FEEDBACK_DIFFICULTY_LEVEL;
        }
        return userDifficultyLevelApi.getDifficultyLevel(userId).name();
    }

    private WritingPracticeSessionResponse toSessionResponse(WritingPracticeSession session, String userId) {
        return WRITING_PRACTICE_API_MAPPER.toResponse(session, usages -> buildFlashcards(userId, usages));
    }

    private WritingPracticeScenario requireScenario(WritingPracticeSession session, String scenarioId) {
        return session.scenarios().stream().filter(value -> value.id().id().equals(scenarioId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Writing scenario not found"));
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

    private List<WritingFeedbackVocabularyItem> buildFeedbackVocabulary(String userId,
                                                                        List<WritingVocabularyUsage> usages) {
        if (usages == null || usages.isEmpty()) {
            return List.of();
        }
        var vocabIds = usages.stream().map(WritingVocabularyUsage::vocabularyId).distinct().toList();
        var records = fetchPrivateVocabularyApi.getVocabularyRecords(vocabIds, userId).stream()
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));

        return usages.stream()
                .map(usage -> {
                    var record = records.get(usage.vocabularyId());
                    if (record == null) {
                        return null;
                    }
                    return new WritingFeedbackVocabularyItem(
                            record.id(),
                            record.surface(),
                            record.translation(),
                            record.entryKind(),
                            true
                    );
                })
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
