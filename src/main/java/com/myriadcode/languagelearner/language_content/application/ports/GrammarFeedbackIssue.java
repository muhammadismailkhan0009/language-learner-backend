package com.myriadcode.languagelearner.language_content.application.ports;

public record GrammarFeedbackIssue(
        String issueText,
        String message,
        String suggestion,
        String ruleIdentifier,
        String fallbackExplanation
) {}
