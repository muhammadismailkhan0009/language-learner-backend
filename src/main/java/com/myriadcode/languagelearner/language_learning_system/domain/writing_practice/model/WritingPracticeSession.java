package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record WritingPracticeSession(
        WritingPracticeSessionId id,
        UserId userId,
        String topic,
        String englishParagraph,
        String germanParagraph,
        Instant createdAt,
        String submittedAnswer,
        Instant submittedAt,
        String feedbackText,
        WritingStructuredFeedback structuredFeedback,
        Instant feedbackGeneratedAt,
        List<WritingSentencePair> sentencePairs,
        List<WritingVocabularyUsage> vocabularyUsages
) {

    public WritingPracticeSession {
    }

    public WritingPracticeSession(
            WritingPracticeSessionId id,
            UserId userId,
            String topic,
            String englishParagraph,
            String germanParagraph,
            Instant createdAt,
            String submittedAnswer,
            Instant submittedAt,
            String feedbackText,
            Instant feedbackGeneratedAt,
            List<WritingSentencePair> sentencePairs,
            List<WritingVocabularyUsage> vocabularyUsages
    ) {
        this(id, userId, topic, englishParagraph, germanParagraph, createdAt, submittedAnswer, submittedAt,
                feedbackText, null, feedbackGeneratedAt, sentencePairs, vocabularyUsages);
    }

    public WritingPracticeSessionId id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String topic() {
        return topic;
    }

    public String englishParagraph() {
        return englishParagraph;
    }

    public String germanParagraph() {
        return germanParagraph;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String submittedAnswer() {
        return submittedAnswer;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public String feedbackText() {
        return feedbackText;
    }

    public WritingStructuredFeedback structuredFeedback() {
        return structuredFeedback;
    }

    public Instant feedbackGeneratedAt() {
        return feedbackGeneratedAt;
    }

    public List<WritingSentencePair> sentencePairs() {
        return sentencePairs;
    }

    public List<WritingVocabularyUsage> vocabularyUsages() {
        return vocabularyUsages;
    }

    public record WritingPracticeSessionId(String id) {
    }
}
