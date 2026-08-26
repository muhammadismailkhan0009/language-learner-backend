package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import java.util.List;

public record WritingPracticeStoreResponse(boolean stored, List<String> validationErrors) {
}
