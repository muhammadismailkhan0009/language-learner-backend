package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "writing_practice_grammar_issue_analytics")
public class WritingGrammarIssueAnalyticsEntity {
    @Id
    private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "grammar_rule_identifier")
    private String grammarRuleIdentifier;

    @Column(name = "issue_type", nullable = false)
    private String issueType;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "learner_text", columnDefinition = "text")
    private String learnerText;

    @Column(name = "corrected_text", columnDefinition = "text")
    private String correctedText;

    @Column(name = "short_explanation", columnDefinition = "text")
    private String shortExplanation;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getGrammarRuleIdentifier() { return grammarRuleIdentifier; }
    public void setGrammarRuleIdentifier(String grammarRuleIdentifier) { this.grammarRuleIdentifier = grammarRuleIdentifier; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getLearnerText() { return learnerText; }
    public void setLearnerText(String learnerText) { this.learnerText = learnerText; }
    public String getCorrectedText() { return correctedText; }
    public void setCorrectedText(String correctedText) { this.correctedText = correctedText; }
    public String getShortExplanation() { return shortExplanation; }
    public void setShortExplanation(String shortExplanation) { this.shortExplanation = shortExplanation; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
