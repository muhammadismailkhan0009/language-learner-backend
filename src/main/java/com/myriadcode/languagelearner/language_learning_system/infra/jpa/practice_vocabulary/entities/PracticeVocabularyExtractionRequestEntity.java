package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "practice_vocabulary_extraction_request")
@Deprecated(forRemoval = true)
public class PracticeVocabularyExtractionRequestEntity {
    @Id @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "source_text", nullable = false, columnDefinition = "text") private String text;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected PracticeVocabularyExtractionRequestEntity() {}

    public PracticeVocabularyExtractionRequestEntity(String userId, String text, Instant createdAt) {
        this.userId = userId; this.text = text; this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
