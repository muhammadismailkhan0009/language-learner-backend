package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "writing_practice_session")
public class WritingPracticeSessionEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "english_paragraph", nullable = false, columnDefinition = "text")
    private String englishParagraph;

    @Column(name = "german_paragraph", nullable = false, columnDefinition = "text")
    private String germanParagraph;

    @Column(name = "submitted_answer", columnDefinition = "text")
    private String submittedAnswer;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "feedback_text", columnDefinition = "text")
    private String feedbackText;

    @Column(name = "structured_feedback_json", columnDefinition = "text")
    private String structuredFeedbackJson;

    @Column(name = "feedback_generated_at")
    private Instant feedbackGeneratedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Fetch(FetchMode.SUBSELECT)
    private Set<WritingPracticeScenarioEntity> scenarios = new LinkedHashSet<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getEnglishParagraph() { return englishParagraph; }
    public void setEnglishParagraph(String englishParagraph) { this.englishParagraph = englishParagraph; }
    public String getGermanParagraph() { return germanParagraph; }
    public void setGermanParagraph(String germanParagraph) { this.germanParagraph = germanParagraph; }
    public String getSubmittedAnswer() { return submittedAnswer; }
    public void setSubmittedAnswer(String submittedAnswer) { this.submittedAnswer = submittedAnswer; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }
    public String getStructuredFeedbackJson() { return structuredFeedbackJson; }
    public void setStructuredFeedbackJson(String structuredFeedbackJson) { this.structuredFeedbackJson = structuredFeedbackJson; }
    public Instant getFeedbackGeneratedAt() { return feedbackGeneratedAt; }
    public void setFeedbackGeneratedAt(Instant feedbackGeneratedAt) { this.feedbackGeneratedAt = feedbackGeneratedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<WritingPracticeScenarioEntity> getScenarios() { return scenarios; }
    public void setScenarios(Set<WritingPracticeScenarioEntity> scenarios) {
        this.scenarios.clear();
        if (scenarios != null) scenarios.forEach(this::addScenario);
    }
    public void addScenario(WritingPracticeScenarioEntity scenario) {
        scenario.setSession(this);
        this.scenarios.add(scenario);
    }
}
