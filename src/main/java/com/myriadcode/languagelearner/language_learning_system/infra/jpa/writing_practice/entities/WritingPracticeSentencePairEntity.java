package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "writing_practice_sentence_pair")
public class WritingPracticeSentencePairEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private WritingPracticeSessionEntity session;

    @Column(name = "sentence_index", nullable = false)
    private int position;

    @Column(name = "english_sentence", nullable = false, columnDefinition = "text")
    private String englishSentence;

    @Column(name = "german_sentence", nullable = false, columnDefinition = "text")
    private String germanSentence;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public WritingPracticeSessionEntity getSession() { return session; }
    public void setSession(WritingPracticeSessionEntity session) { this.session = session; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getEnglishSentence() { return englishSentence; }
    public void setEnglishSentence(String englishSentence) { this.englishSentence = englishSentence; }
    public String getGermanSentence() { return germanSentence; }
    public void setGermanSentence(String germanSentence) { this.germanSentence = germanSentence; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
