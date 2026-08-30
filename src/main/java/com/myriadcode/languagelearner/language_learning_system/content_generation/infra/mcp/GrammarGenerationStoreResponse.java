package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import java.util.List;

public record GrammarGenerationStoreResponse(boolean success, List<String> validationErrors, Integer stored) {}
