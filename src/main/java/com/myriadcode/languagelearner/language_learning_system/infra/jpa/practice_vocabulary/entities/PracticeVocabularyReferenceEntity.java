package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "practice_vocabulary_reference",
        uniqueConstraints = @UniqueConstraint(name = "uk_practice_vocab_ref_user_vocab", columnNames = {"user_id", "vocabulary_id"}))
public class PracticeVocabularyReferenceEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "vocabulary_id", nullable = false)
    private String vocabularyId;

    @Column(name = "times_matched", nullable = false)
    private int timesMatched;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        var now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public int getTimesMatched() {
        return timesMatched;
    }

    public void setTimesMatched(int timesMatched) {
        this.timesMatched = timesMatched;
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
