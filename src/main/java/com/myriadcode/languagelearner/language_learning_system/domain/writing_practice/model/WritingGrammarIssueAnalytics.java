package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;

public record WritingGrammarIssueAnalytics(
        WritingGrammarIssueAnalyticsId id,
        WritingPracticeSession.WritingPracticeSessionId sessionId,
        UserId userId,
        String grammarRuleIdentifier,
        String issueType,
        int priority,
        String learnerText,
        String correctedText,
        String shortExplanation,
        int occurrenceCount,
        Instant createdAt
) {
    public record WritingGrammarIssueAnalyticsId(String id) {
    }
}
