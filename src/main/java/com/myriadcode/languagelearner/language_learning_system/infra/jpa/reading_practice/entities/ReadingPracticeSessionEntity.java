package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "reading_practice_session")
public class ReadingPracticeSessionEntity {

    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "reading_text", nullable = false, columnDefinition = "text")
    private String readingText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "session_id", nullable = false)
    @OrderColumn(name = "scenario_index")
    private List<ReadingPracticeScenarioEntity> scenarios = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getReadingText() {
        return readingText;
    }

    public void setReadingText(String readingText) {
        this.readingText = readingText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReadingPracticeScenarioEntity> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<ReadingPracticeScenarioEntity> values) {
        scenarios.clear(); if (values != null) scenarios.addAll(values);
    }

    public List<ReadingPracticeParagraphEntity> getParagraphs() {
        return scenarios.isEmpty() ? List.of() : scenarios.getFirst().getParagraphs();
    }

    public Set<ReadingPracticeVocabularyUsageEntity> getVocabularyUsages() {
        return scenarios.stream().flatMap(scenario -> scenario.getVocabularyUsages().stream())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public void addVocabularyUsage(ReadingPracticeVocabularyUsageEntity usage) {
        ensureCompatibilityScenario().addVocabularyUsage(usage);
    }

    private ReadingPracticeScenarioEntity ensureCompatibilityScenario() {
        if (!scenarios.isEmpty()) return scenarios.getFirst();
        var scenario = new ReadingPracticeScenarioEntity();
        scenario.setId((id == null ? "session" : id) + "-scenario");
        scenario.setLabel(topic == null ? "Reading practice" : topic);
        scenario.setReadingText(readingText == null ? "" : readingText);
        scenario.setCreatedAt(createdAt == null ? Instant.now() : createdAt);
        scenarios.add(scenario);
        return scenario;
    }
}
