package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "mcp_credentials")
class McpCredentialEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "secret_key", nullable = false, unique = true)
    private String secretKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected McpCredentialEntity() {
    }

    McpCredentialEntity(String userId, String secretKey, Instant createdAt) {
        this.userId = userId;
        this.secretKey = secretKey;
        this.createdAt = createdAt;
    }

    String getUserId() { return userId; }
    String getSecretKey() { return secretKey; }
    Instant getCreatedAt() { return createdAt; }
}
