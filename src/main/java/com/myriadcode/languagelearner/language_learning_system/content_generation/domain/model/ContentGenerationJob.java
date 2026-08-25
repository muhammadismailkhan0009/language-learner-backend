package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model;

import java.time.Instant;

public record ContentGenerationJob(String userId, ContentGenerationJobType type, Instant createdAt) {
    public ContentGenerationJob {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Content generation job type is required");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Content generation job creation time is required");
        }
    }
}
