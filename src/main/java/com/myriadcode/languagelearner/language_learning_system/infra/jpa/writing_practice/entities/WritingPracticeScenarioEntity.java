package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "writing_practice_scenario")
public class WritingPracticeScenarioEntity {
    @Id private String id;
    @ManyToOne @JoinColumn(name = "session_id", nullable = false) private WritingPracticeSessionEntity session;
    @Column(name = "scenario_position", nullable = false) private int position;
    @Column(name = "topic", nullable = false) private String topic;
    @Column(name = "english_paragraph", nullable = false, columnDefinition = "text") private String englishParagraph;
    @Column(name = "german_paragraph", nullable = false, columnDefinition = "text") private String germanParagraph;
    @Column(name = "submitted_answer", columnDefinition = "text") private String submittedAnswer;
    @Column(name = "submitted_at") private Instant submittedAt;
    @Column(name = "feedback_text", columnDefinition = "text") private String feedbackText;
    @Column(name = "structured_feedback_json", columnDefinition = "text") private String structuredFeedbackJson;
    @Column(name = "feedback_generated_at") private Instant feedbackGeneratedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC") @Fetch(FetchMode.SUBSELECT)
    private Set<WritingPracticeSentencePairEntity> sentencePairs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC") @Fetch(FetchMode.SUBSELECT)
    private Set<WritingPracticeVocabularyUsageEntity> vocabularyUsages = new LinkedHashSet<>();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public WritingPracticeSessionEntity getSession() { return session; } public void setSession(WritingPracticeSessionEntity session) { this.session = session; }
    public int getPosition() { return position; } public void setPosition(int position) { this.position = position; }
    public String getTopic() { return topic; } public void setTopic(String topic) { this.topic = topic; }
    public String getEnglishParagraph() { return englishParagraph; } public void setEnglishParagraph(String value) { englishParagraph = value; }
    public String getGermanParagraph() { return germanParagraph; } public void setGermanParagraph(String value) { germanParagraph = value; }
    public String getSubmittedAnswer() { return submittedAnswer; } public void setSubmittedAnswer(String value) { submittedAnswer = value; }
    public Instant getSubmittedAt() { return submittedAt; } public void setSubmittedAt(Instant value) { submittedAt = value; }
    public String getFeedbackText() { return feedbackText; } public void setFeedbackText(String value) { feedbackText = value; }
    public String getStructuredFeedbackJson() { return structuredFeedbackJson; } public void setStructuredFeedbackJson(String value) { structuredFeedbackJson = value; }
    public Instant getFeedbackGeneratedAt() { return feedbackGeneratedAt; } public void setFeedbackGeneratedAt(Instant value) { feedbackGeneratedAt = value; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant value) { createdAt = value; }
    public Set<WritingPracticeSentencePairEntity> getSentencePairs() { return sentencePairs; }
    public Set<WritingPracticeVocabularyUsageEntity> getVocabularyUsages() { return vocabularyUsages; }
    public void addSentencePair(WritingPracticeSentencePairEntity value) { value.setSession(session); value.setScenario(this); sentencePairs.add(value); }
    public void addVocabularyUsage(WritingPracticeVocabularyUsageEntity value) { value.setSession(session); value.setScenario(this); vocabularyUsages.add(value); }
}
