package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions;

public class InsufficientWordPracticeCandidatesException extends RuntimeException {

    private final int availableVocabularyCount;
    private final int requiredVocabularyCount;

    public InsufficientWordPracticeCandidatesException(int availableVocabularyCount, int requiredVocabularyCount) {
        super("Not enough eligible vocabulary for Word Practice generation");
        this.availableVocabularyCount = availableVocabularyCount;
        this.requiredVocabularyCount = requiredVocabularyCount;
    }

    public int getAvailableVocabularyCount() {
        return availableVocabularyCount;
    }

    public int getRequiredVocabularyCount() {
        return requiredVocabularyCount;
    }
}
