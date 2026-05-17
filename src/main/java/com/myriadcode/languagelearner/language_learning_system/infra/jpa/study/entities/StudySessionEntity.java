package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "study_session")
public class StudySessionEntity {
    @Id
    private String id;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
