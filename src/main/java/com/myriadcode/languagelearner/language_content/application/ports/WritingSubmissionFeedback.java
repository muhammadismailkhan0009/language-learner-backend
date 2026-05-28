package com.myriadcode.languagelearner.language_content.application.ports;

public record WritingSubmissionFeedback(
        String feedback,
        java.util.List<GrammarFeedbackIssue> grammarIssues
) {
    public WritingSubmissionFeedback(String feedback) {
        this(feedback, java.util.List.of());
    }
}
