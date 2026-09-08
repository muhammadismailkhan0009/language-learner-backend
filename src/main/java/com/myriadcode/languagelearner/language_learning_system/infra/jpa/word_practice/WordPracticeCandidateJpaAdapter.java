package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.WordPracticeCandidateProvider;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
class WordPracticeCandidateJpaAdapter implements WordPracticeCandidateProvider {
    private final VocabularyEntityJpaRepo vocabularies;
    private final WordPracticeJpaRepository practices;
    private final RecentExerciseVocabularyUsageService recentUsage;

    WordPracticeCandidateJpaAdapter(VocabularyEntityJpaRepo vocabularies,
                                    WordPracticeJpaRepository practices,
                                    RecentExerciseVocabularyUsageService recentUsage) {
        this.vocabularies = vocabularies;
        this.practices = practices;
        this.recentUsage = recentUsage;
    }

    public Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> findRankedCandidates(
            String userId, int lowExposureMax) {
        var vocabulary = vocabularies.findAllByUserId(userId);
        var activeVocabulary = practices.findActiveVocabularyIds(userId);
        var exposure = recentUsage.countRecentSessionUsage(userId);
        var result = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>>(
                WordPracticeSelectionCategory.class);
        result.put(WordPracticeSelectionCategory.NEW, vocabulary.stream()
                .filter(v -> !activeVocabulary.contains(v.getId()))
                .sorted(Comparator.comparing(VocabularyEntity::getCreatedAt).thenComparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.NEW)).toList());
        result.put(WordPracticeSelectionCategory.LOW_EXPOSURE, vocabulary.stream()
                .filter(v -> exposure.getOrDefault(v.getId(), 0) < lowExposureMax)
                .sorted(Comparator.comparingInt((VocabularyEntity v) -> exposure.getOrDefault(v.getId(), 0))
                        .thenComparing(VocabularyEntity::getCreatedAt).thenComparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.LOW_EXPOSURE)).toList());
        result.put(WordPracticeSelectionCategory.STALE, List.of());
        result.put(WordPracticeSelectionCategory.RANDOM, vocabulary.stream()
                .sorted(Comparator.comparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.RANDOM)).toList());
        return Map.copyOf(result);
    }

    private WordPracticeGenerationCandidate candidate(VocabularyEntity value, WordPracticeSelectionCategory category) {
        return new WordPracticeGenerationCandidate(new WordPracticeCandidate(value.getId(), category),
                value.getSurface(), value.getTranslation());
    }
}
