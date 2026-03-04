package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reading_practice_vocab_ref")
public class ReadingPracticeVocabularyUsageEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingPracticeSessionEntity session;

    @Column(name = "flashcard_id", nullable = false)
    private String flashcardId;

    @Column(name = "vocabulary_id", nullable = false)
    private String vocabularyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ReadingPracticeSessionEntity getSession() {
        return session;
    }

    public void setSession(ReadingPracticeSessionEntity session) {
        this.session = session;
    }

    public String getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(String flashcardId) {
        this.flashcardId = flashcardId;
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(String vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
