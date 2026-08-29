package com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary;

import java.util.List;

@Deprecated(forRemoval = true)
public class PracticeVocabularyExtractionValidationException extends IllegalArgumentException {
    private final List<String> validationErrors;

    public PracticeVocabularyExtractionValidationException(List<String> validationErrors) {
        super(String.join("; ", validationErrors));
        this.validationErrors = List.copyOf(validationErrors);
    }

    public List<String> validationErrors() { return validationErrors; }
}
