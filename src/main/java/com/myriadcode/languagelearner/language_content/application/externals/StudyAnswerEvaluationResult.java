package com.myriadcode.languagelearner.language_content.application.externals;

public record StudyAnswerEvaluationResult(
        double semanticMatch,
        double formAccuracy,
        double confidence,
        String feedback,
        java.util.List<GrammarFeedbackIssueResult> grammarIssues
) {
    public StudyAnswerEvaluationResult(
            double semanticMatch,
            double formAccuracy,
            double confidence,
            String feedback
    ) {
        this(semanticMatch, formAccuracy, confidence, feedback, java.util.List.of());
    }
}
