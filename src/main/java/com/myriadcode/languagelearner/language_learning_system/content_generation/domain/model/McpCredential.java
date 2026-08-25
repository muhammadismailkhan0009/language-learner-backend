package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model;

import java.time.Instant;

public record McpCredential(String userId, String secretKey, Instant createdAt) {
    public McpCredential {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("MCP secret key is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("MCP credential creation time is required");
        }
    }
}
