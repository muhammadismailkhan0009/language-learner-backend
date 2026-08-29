package com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitVocabularyExtractionRequest(
        @NotBlank String userId,
        @NotBlank String sourceText
) {
}

