package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.WordPracticeCandidateProvider;
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
    private final FetchVocabularyFlashcardReviewsApi flashcards;

    WordPracticeCandidateJpaAdapter(VocabularyEntityJpaRepo vocabularies,
                                    WordPracticeJpaRepository practices,
                                    FetchVocabularyFlashcardReviewsApi flashcards) {
        this.vocabularies = vocabularies;
        this.practices = practices;
        this.flashcards = flashcards;
    }

    public Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> findRankedCandidates(String userId) {
        var vocabularyById = vocabularies.findAllByUserId(userId).stream()
                .collect(java.util.stream.Collectors.toMap(VocabularyEntity::getId, value -> value));
        var activeVocabulary = practices.findActiveVocabularyIds(userId);
        var vocabularyCards = flashcards.getVocabularyFlashcardsByUser(userId);
        var result = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>>(
                WordPracticeSelectionCategory.class);
        for (var category : WordPracticeSelectionCategory.values()) {
            result.put(category, vocabularyCards.stream()
                    .filter(review -> review.isReversed() && review.fsrsState() != null)
                    .filter(review -> review.fsrsState().name().equals(category.name()))
                    .filter(review -> !activeVocabulary.contains(review.vocabularyId()))
                    .map(review -> vocabularyById.get(review.vocabularyId()))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(VocabularyEntity::getCreatedAt).thenComparing(VocabularyEntity::getId))
                    .map(value -> candidate(value, category))
                    .toList());
        }
        return Map.copyOf(result);
    }

    private WordPracticeGenerationCandidate candidate(VocabularyEntity value, WordPracticeSelectionCategory category) {
        return new WordPracticeGenerationCandidate(new WordPracticeCandidate(value.getId(), category),
                value.getSurface(), value.getTranslation());
    }
}
