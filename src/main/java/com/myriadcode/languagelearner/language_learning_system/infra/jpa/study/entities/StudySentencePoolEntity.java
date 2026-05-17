package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "study_sentence_pool")
public class StudySentencePoolEntity {
    @Id
    private String id;
    @Column(name = "vocabulary_id", nullable = false)
    private String vocabularyId;
    @Column(name = "sentence_text_with_blank", nullable = false, columnDefinition = "text")
    private String sentenceTextWithBlank;
    @Column(name = "true_answer_surface", nullable = false)
    private String trueAnswerSurface;
    @Column(name = "normalized_true_answer", nullable = false)
    private String normalizedTrueAnswer;
    @Column(name = "hint")
    private String hint;
    @Column(name = "source", nullable = false)
    private String source;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() { if (createdAt == null) createdAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }
    public String getSentenceTextWithBlank() { return sentenceTextWithBlank; }
    public void setSentenceTextWithBlank(String sentenceTextWithBlank) { this.sentenceTextWithBlank = sentenceTextWithBlank; }
    public String getTrueAnswerSurface() { return trueAnswerSurface; }
    public void setTrueAnswerSurface(String trueAnswerSurface) { this.trueAnswerSurface = trueAnswerSurface; }
    public String getNormalizedTrueAnswer() { return normalizedTrueAnswer; }
    public void setNormalizedTrueAnswer(String normalizedTrueAnswer) { this.normalizedTrueAnswer = normalizedTrueAnswer; }
    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
