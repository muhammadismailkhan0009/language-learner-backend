package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.WordPracticeCapacityExceededException;

public class WordPracticeCapacityPolicy {

    public static final int MAXIMUM_ACTIVE_VOCABULARY = 15;
    public static final int GENERATION_VOCABULARY_COUNT = 10;

    public void requireCapacity(int activeVocabularyCount, int requestedVocabularyCount) {
        if (activeVocabularyCount < 0 || requestedVocabularyCount < 0) {
            throw new IllegalArgumentException("Vocabulary counts must not be negative");
        }
        if (activeVocabularyCount + requestedVocabularyCount > MAXIMUM_ACTIVE_VOCABULARY) {
            throw new WordPracticeCapacityExceededException(
                    activeVocabularyCount,
                    requestedVocabularyCount,
                    MAXIMUM_ACTIVE_VOCABULARY
            );
        }
    }
}
