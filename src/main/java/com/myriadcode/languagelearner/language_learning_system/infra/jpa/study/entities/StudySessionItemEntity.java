package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "study_session_item")
public class StudySessionItemEntity {
    @Id
    private String id;
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    @Column(name = "flashcard_id", nullable = false)
    private String flashcardId;
    @Column(name = "vocabulary_id", nullable = false)
    private String vocabularyId;
    @Column(name = "sentence_id", nullable = false)
    private String sentenceId;
    @Column(name = "queue_rank_snapshot", nullable = false)
    private int queueRankSnapshot;
    @Column(name = "presented_at", nullable = false)
    private Instant presentedAt;
    @Column(name = "rated_at")
    private Instant ratedAt;
    @Column(name = "rating_applied")
    private String ratingApplied;
    @Column(name = "answer_text", columnDefinition = "text")
    private String answerText;
    @Column(name = "evaluation_mode")
    private String evaluationMode;
    @Column(name = "feedback_text", columnDefinition = "text")
    private String feedbackText;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getFlashcardId() { return flashcardId; }
    public void setFlashcardId(String flashcardId) { this.flashcardId = flashcardId; }
    public String getVocabularyId() { return vocabularyId; }
    public void setVocabularyId(String vocabularyId) { this.vocabularyId = vocabularyId; }
    public String getSentenceId() { return sentenceId; }
    public void setSentenceId(String sentenceId) { this.sentenceId = sentenceId; }
    public int getQueueRankSnapshot() { return queueRankSnapshot; }
    public void setQueueRankSnapshot(int queueRankSnapshot) { this.queueRankSnapshot = queueRankSnapshot; }
    public Instant getPresentedAt() { return presentedAt; }
    public void setPresentedAt(Instant presentedAt) { this.presentedAt = presentedAt; }
    public Instant getRatedAt() { return ratedAt; }
    public void setRatedAt(Instant ratedAt) { this.ratedAt = ratedAt; }
    public String getRatingApplied() { return ratingApplied; }
    public void setRatingApplied(String ratingApplied) { this.ratingApplied = ratingApplied; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getEvaluationMode() { return evaluationMode; }
    public void setEvaluationMode(String evaluationMode) { this.evaluationMode = evaluationMode; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
}
