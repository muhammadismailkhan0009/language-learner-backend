package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionResult;

import java.util.List;

public record VocabularyExtractionStoreResponse(
        boolean stored,
        List<String> validationErrors,
        VocabularyExtractionResult result
) {}
