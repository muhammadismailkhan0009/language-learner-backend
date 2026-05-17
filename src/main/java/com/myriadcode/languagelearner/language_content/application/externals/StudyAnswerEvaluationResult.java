package com.myriadcode.languagelearner.language_content.application.externals;

public record StudyAnswerEvaluationResult(
        double semanticMatch,
        double formAccuracy,
        double confidence,
        String feedback
) {
}
