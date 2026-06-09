package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingGrammarIssueDetectionResult(List<Issue> issues) {
    public record Issue(
            String grammarRuleIdentifier,
            String issueType,
            int priority,
            String learnerText,
            String correctedText,
            String shortExplanation,
            boolean topCandidate,
            int occurrenceCount
    ) {
    }
}
