package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import java.util.List;

public class WritingPracticeGenerationValidationException extends IllegalArgumentException {
    private final List<String> validationErrors;

    public WritingPracticeGenerationValidationException(List<String> validationErrors) {
        super(String.join("; ", validationErrors));
        this.validationErrors = List.copyOf(validationErrors);
    }

    public List<String> validationErrors() {
        return validationErrors;
    }
}
