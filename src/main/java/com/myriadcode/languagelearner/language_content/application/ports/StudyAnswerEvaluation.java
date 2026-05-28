package com.myriadcode.languagelearner.language_content.application.ports;

public record StudyAnswerEvaluation(
        double semanticMatch,
        double formAccuracy,
        double confidence,
        String feedback,
        java.util.List<GrammarFeedbackIssue> grammarIssues
) {
    public StudyAnswerEvaluation(double semanticMatch,
                                 double formAccuracy,
                                 double confidence,
                                 String feedback) {
        this(semanticMatch, formAccuracy, confidence, feedback, java.util.List.of());
    }
}
