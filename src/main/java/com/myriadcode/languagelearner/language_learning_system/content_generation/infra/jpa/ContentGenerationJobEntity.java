package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "content_generation_jobs")
@IdClass(ContentGenerationJobEntityId.class)
class ContentGenerationJobEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Id
    @Column(name = "type", nullable = false, length = 64)
    private ContentGenerationJobType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ContentGenerationJobEntity() {
    }

    ContentGenerationJobEntity(String userId, ContentGenerationJobType type, Instant createdAt) {
        this.userId = userId;
        this.type = type;
        this.createdAt = createdAt;
    }

    String getUserId() { return userId; }
    ContentGenerationJobType getType() { return type; }
    Instant getCreatedAt() { return createdAt; }
}
