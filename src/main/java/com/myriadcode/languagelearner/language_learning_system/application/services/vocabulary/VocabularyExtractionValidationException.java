package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import java.util.List;

public class VocabularyExtractionValidationException extends RuntimeException {
    private final List<String> validationErrors;

    public VocabularyExtractionValidationException(List<String> validationErrors) {
        super(String.join("; ", validationErrors));
        this.validationErrors = List.copyOf(validationErrors);
    }

    public List<String> validationErrors() { return validationErrors; }
}
