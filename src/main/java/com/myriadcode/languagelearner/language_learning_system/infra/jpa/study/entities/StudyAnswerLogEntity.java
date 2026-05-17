package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "study_answer_log")
public class StudyAnswerLogEntity {
    @Id
    private String id;
    @Column(name = "session_item_id", nullable = false)
    private String sessionItemId;
    @Column(name = "user_answer", nullable = false, columnDefinition = "text")
    private String userAnswer;
    @Column(name = "normalized_user_answer", nullable = false)
    private String normalizedUserAnswer;
    @Column(name = "is_exact_match", nullable = false)
    private boolean exactMatch;
    @Column(name = "llm_payload_json", columnDefinition = "text")
    private String llmPayloadJson;
    @Column(name = "mapped_rating", nullable = false)
    private String mappedRating;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionItemId() { return sessionItemId; }
    public void setSessionItemId(String sessionItemId) { this.sessionItemId = sessionItemId; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public String getNormalizedUserAnswer() { return normalizedUserAnswer; }
    public void setNormalizedUserAnswer(String normalizedUserAnswer) { this.normalizedUserAnswer = normalizedUserAnswer; }
    public boolean isExactMatch() { return exactMatch; }
    public void setExactMatch(boolean exactMatch) { this.exactMatch = exactMatch; }
    public String getLlmPayloadJson() { return llmPayloadJson; }
    public void setLlmPayloadJson(String llmPayloadJson) { this.llmPayloadJson = llmPayloadJson; }
    public String getMappedRating() { return mappedRating; }
    public void setMappedRating(String mappedRating) { this.mappedRating = mappedRating; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
