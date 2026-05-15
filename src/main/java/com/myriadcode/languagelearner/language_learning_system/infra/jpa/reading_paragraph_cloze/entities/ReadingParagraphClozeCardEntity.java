package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "reading_paragraph_cloze_card")
public class ReadingParagraphClozeCardEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingParagraphClozeSessionEntity session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragraph_id")
    private ReadingParagraphClozeParagraphEntity paragraph;

    @Column(name = "paragraph_id", insertable = false, updatable = false)
    private String paragraphId;

    @Column(name = "flashcard_id", nullable = false)
    private String flashcardId;

    @Column(name = "vocabulary_id", nullable = false)
    private String vocabularyId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public ReadingParagraphClozeSessionEntity getSession() { return session; }
    public void setSession(ReadingParagraphClozeSessionEntity session) { this.session = session; }
    public ReadingParagraphClozeParagraphEntity getParagraph() { return paragraph; }
    public void setParagraph(ReadingParagraphClozeParagraphEntity paragraph) { this.paragraph = paragraph; }
    public String getParagraphId() { return paragraphId; }
    public String getFlashcardId() { return flashcardId; }
    public void setFlashcardId(String flashcardId) { this.flashcardId = flashcardId; }
    public String getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
