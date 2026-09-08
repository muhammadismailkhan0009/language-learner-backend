package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions;

public class WordPracticeCapacityExceededException extends RuntimeException {

    private final int activeVocabularyCount;
    private final int requestedVocabularyCount;
    private final int maximumVocabularyCount;

    public WordPracticeCapacityExceededException(int activeVocabularyCount,
                                                 int requestedVocabularyCount,
                                                 int maximumVocabularyCount) {
        super("Word Practice capacity exceeded");
        this.activeVocabularyCount = activeVocabularyCount;
        this.requestedVocabularyCount = requestedVocabularyCount;
        this.maximumVocabularyCount = maximumVocabularyCount;
    }

    public int getActiveVocabularyCount() {
        return activeVocabularyCount;
    }

    public int getRequestedVocabularyCount() {
        return requestedVocabularyCount;
    }

    public int getMaximumVocabularyCount() {
        return maximumVocabularyCount;
    }
}
