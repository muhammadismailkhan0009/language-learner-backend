package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ReadingPracticeVocabularyUsageEntity> vocabularyUsages = new ArrayList<>();

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

    public List<ReadingPracticeVocabularyUsageEntity> getVocabularyUsages() {
        return vocabularyUsages;
    }

    public void setVocabularyUsages(List<ReadingPracticeVocabularyUsageEntity> vocabularyUsages) {
        this.vocabularyUsages.clear();
        if (vocabularyUsages == null) {
            return;
        }
        for (ReadingPracticeVocabularyUsageEntity usage : vocabularyUsages) {
            addVocabularyUsage(usage);
        }
    }

    public void addVocabularyUsage(ReadingPracticeVocabularyUsageEntity usage) {
        usage.setSession(this);
        this.vocabularyUsages.add(usage);
    }
}
