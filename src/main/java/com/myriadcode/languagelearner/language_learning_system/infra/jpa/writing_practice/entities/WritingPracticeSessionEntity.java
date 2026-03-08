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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @Fetch(FetchMode.SUBSELECT)
    private Set<WritingPracticeSentencePairEntity> sentencePairs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Fetch(FetchMode.SUBSELECT)
    private Set<WritingPracticeVocabularyUsageEntity> vocabularyUsages = new LinkedHashSet<>();

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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<WritingPracticeSentencePairEntity> getSentencePairs() { return sentencePairs; }
    public void setSentencePairs(Set<WritingPracticeSentencePairEntity> sentencePairs) {
        this.sentencePairs.clear();
        if (sentencePairs == null) {
            return;
        }
        sentencePairs.forEach(this::addSentencePair);
    }
    public void addSentencePair(WritingPracticeSentencePairEntity sentencePair) {
        sentencePair.setSession(this);
        this.sentencePairs.add(sentencePair);
    }
    public Set<WritingPracticeVocabularyUsageEntity> getVocabularyUsages() { return vocabularyUsages; }
    public void setVocabularyUsages(Set<WritingPracticeVocabularyUsageEntity> vocabularyUsages) {
        this.vocabularyUsages.clear();
        if (vocabularyUsages == null) {
            return;
        }
        vocabularyUsages.forEach(this::addVocabularyUsage);
    }
    public void addVocabularyUsage(WritingPracticeVocabularyUsageEntity usage) {
        usage.setSession(this);
        this.vocabularyUsages.add(usage);
    }
}
