package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "vocabulary_extraction_request")
public class VocabularyExtractionRequestEntity {
    @Id private String id;
    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "source_text", nullable = false) private String sourceText;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected VocabularyExtractionRequestEntity() {}

    public VocabularyExtractionRequestEntity(String id, String userId, String sourceText, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.sourceText = sourceText;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getSourceText() { return sourceText; }
    public Instant getCreatedAt() { return createdAt; }
}
