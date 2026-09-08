package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.WordPracticeCandidateProvider;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeBatchValidator;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeCapacityPolicy;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeSelectionPolicy;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;
import static com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services.WordPracticeCapacityPolicy.GENERATION_VOCABULARY_COUNT;
import static com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator.wordPracticeGeneration;

@Service
public class WordPracticeGenerationService {
    private final WordPracticeRepo practiceRepo;
    private final WordPracticeCandidateProvider candidateProvider;
    private final ContentGenerationJobService jobService;
    private final UserDifficultyLevelApi userDifficultyLevelApi;
    private final Clock clock;
    private final WordPracticeCapacityPolicy capacityPolicy = new WordPracticeCapacityPolicy();
    private final WordPracticeSelectionPolicy selectionPolicy = new WordPracticeSelectionPolicy();
    private final WordPracticeBatchValidator batchValidator = new WordPracticeBatchValidator();

    public WordPracticeGenerationService(WordPracticeRepo practiceRepo,
                                         WordPracticeCandidateProvider candidateProvider,
                                         ContentGenerationJobService jobService,
                                         UserDifficultyLevelApi userDifficultyLevelApi) {
        this(practiceRepo, candidateProvider, jobService, userDifficultyLevelApi, Clock.systemUTC());
    }

    WordPracticeGenerationService(WordPracticeRepo practiceRepo,
                                  WordPracticeCandidateProvider candidateProvider,
                                  ContentGenerationJobService jobService,
                                  UserDifficultyLevelApi userDifficultyLevelApi,
                                  Clock clock) {
        this.practiceRepo = practiceRepo;
        this.candidateProvider = candidateProvider;
        this.jobService = jobService;
        this.userDifficultyLevelApi = userDifficultyLevelApi;
        this.clock = clock;
    }

    @Transactional
    public List<WordPracticeGenerationCandidate> prepare(String userId) {
        jobService.require(userId, WORD_PRACTICE);
        requireCapacity(userId);
        var candidatesByCategory = candidateProvider.findRankedCandidates(userId);
        var detailsById = flatten(candidatesByCategory).stream().collect(Collectors.toMap(
                WordPracticeGenerationCandidate::vocabularyId,
                Function.identity(),
                (first, ignored) -> first
        ));
        var selected = selectionPolicy.select(toSelections(candidatesByCategory),
                practiceRepo.findActiveVocabularyIds(userId), new Random());
        var selectedDetails = selected.stream().map(candidate -> detailsById.get(candidate.vocabularyId())).toList();
        return selectedDetails;
    }

    @Transactional
    public String prepareGenerationPrompt(String userId) {
        return wordPracticeGeneration(prepare(userId), userDifficultyLevelApi.getDifficultyLevel(userId));
    }

    @Transactional
    public int store(String userId, List<WordPracticeCandidate> selected,
                     List<GeneratedWordPracticeGroup> groups) {
        jobService.require(userId, WORD_PRACTICE);
        requireCapacity(userId);
        batchValidator.validate(selected.stream().map(WordPracticeCandidate::vocabularyId).toList(), groups);
        practiceRepo.saveGeneration(userId, groups, clock.instant());
        jobService.delete(userId, WORD_PRACTICE);
        return groups.stream().mapToInt(group -> group.practices().size()).sum();
    }

    private void requireCapacity(String userId) {
        capacityPolicy.requireCapacity(practiceRepo.countDistinctActiveVocabulary(userId), GENERATION_VOCABULARY_COUNT);
    }

    private List<WordPracticeGenerationCandidate> flatten(
            Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> candidates) {
        return candidates.values().stream().flatMap(List::stream).toList();
    }

    private Map<WordPracticeSelectionCategory, List<WordPracticeCandidate>> toSelections(
            Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> candidates) {
        var result = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeCandidate>>(WordPracticeSelectionCategory.class);
        candidates.forEach((category, values) -> result.put(category,
                values.stream().map(WordPracticeGenerationCandidate::selection).toList()));
        return result;
    }
}
