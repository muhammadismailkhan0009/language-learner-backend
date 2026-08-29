package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;

public record VocabularyExtractionRequest(
        VocabularyExtractionRequestId id,
        UserId userId,
        String sourceText,
        Instant createdAt
) {
    public record VocabularyExtractionRequestId(String id) {}
}
