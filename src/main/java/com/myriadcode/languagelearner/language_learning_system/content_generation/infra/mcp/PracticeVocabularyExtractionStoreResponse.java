package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.ExtractPracticeVocabularyResult;

import java.util.List;

public record PracticeVocabularyExtractionStoreResponse(
        boolean stored,
        List<String> validationErrors,
        ExtractPracticeVocabularyResult result
) {
}
