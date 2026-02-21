package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "public_vocabularies")
public class PublicVocabularyEntity {

    @Id
    private String id;

    @Column(name = "source_vocabulary_id", nullable = false, unique = true)
    private String sourceVocabularyId;

    @Column(name = "published_by_user_id", nullable = false)
    private String publishedByUserId;

    @Column(nullable = false)
    private String status;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        var now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceVocabularyId() {
        return sourceVocabularyId;
    }

    public void setSourceVocabularyId(String sourceVocabularyId) {
        this.sourceVocabularyId = sourceVocabularyId;
    }

    public String getPublishedByUserId() {
        return publishedByUserId;
    }

    public void setPublishedByUserId(String publishedByUserId) {
        this.publishedByUserId = publishedByUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
