package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "vocabulary_extraction_candidate")
public class VocabularyExtractionCandidateEntity {
    @Id private String id;
    @Column(name = "user_id", nullable = false) private String userId;
    @Column(name = "surface", nullable = false, length = 500) private String surface;
    @Column(name = "normalized_surface", nullable = false, length = 500) private String normalizedSurface;
    @Column(name = "created_vocabulary_id") private String createdVocabularyId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected VocabularyExtractionCandidateEntity() {}

    public VocabularyExtractionCandidateEntity(String id, String userId, String surface, String normalizedSurface,
                                                       String createdVocabularyId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.surface = surface;
        this.normalizedSurface = normalizedSurface;
        this.createdVocabularyId = createdVocabularyId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getSurface() { return surface; }
    public String getNormalizedSurface() { return normalizedSurface; }
    public String getCreatedVocabularyId() { return createdVocabularyId; }
    public Instant getCreatedAt() { return createdAt; }
}
