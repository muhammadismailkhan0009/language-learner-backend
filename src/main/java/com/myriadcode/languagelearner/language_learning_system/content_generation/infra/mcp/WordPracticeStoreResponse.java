package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import java.util.List;

public record WordPracticeStoreResponse(
        boolean stored,
        int storedPracticeCount,
        List<String> validationErrors
) {
}
