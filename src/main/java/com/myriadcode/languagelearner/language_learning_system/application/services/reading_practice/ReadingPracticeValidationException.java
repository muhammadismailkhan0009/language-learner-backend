package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import java.util.List;

public class ReadingPracticeValidationException extends IllegalArgumentException {
    private final List<String> validationErrors;

    public ReadingPracticeValidationException(List<String> validationErrors) {
        super("Invalid generated reading content: " + String.join("; ", validationErrors));
        this.validationErrors = List.copyOf(validationErrors);
    }

    public List<String> validationErrors() {
        return validationErrors;
    }
}
