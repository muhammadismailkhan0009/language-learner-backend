package com.myriadcode.languagelearner.language_content.application.externals;

public record GrammarFeedbackIssueResult(
        String issueText,
        String message,
        String suggestion,
        String ruleIdentifier,
        String fallbackExplanation
) {}
