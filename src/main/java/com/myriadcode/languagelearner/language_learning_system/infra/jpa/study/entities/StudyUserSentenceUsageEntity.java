package com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "study_user_sentence_usage", uniqueConstraints = @UniqueConstraint(name = "uk_study_user_sentence", columnNames = {"user_id", "sentence_id"}))
public class StudyUserSentenceUsageEntity {
    @Id
    private String id;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(name = "sentence_id", nullable = false)
    private String sentenceId;
    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;
    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;
    @Column(name = "times_shown", nullable = false)
    private int timesShown;
    @Column(name = "times_correct", nullable = false)
    private int timesCorrect;
    @Column(name = "times_wrong", nullable = false)
    private int timesWrong;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSentenceId() { return sentenceId; }
    public void setSentenceId(String sentenceId) { this.sentenceId = sentenceId; }
    public Instant getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(Instant firstSeenAt) { this.firstSeenAt = firstSeenAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public int getTimesShown() { return timesShown; }
    public void setTimesShown(int timesShown) { this.timesShown = timesShown; }
    public int getTimesCorrect() { return timesCorrect; }
    public void setTimesCorrect(int timesCorrect) { this.timesCorrect = timesCorrect; }
    public int getTimesWrong() { return timesWrong; }
    public void setTimesWrong(int timesWrong) { this.timesWrong = timesWrong; }
}
