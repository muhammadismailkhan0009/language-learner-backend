package com.myriadcode.languagelearner.language_content.application.ports;

public record StudyAnswerEvaluation(
        double semanticMatch,
        double formAccuracy,
        double confidence,
        String feedback
) {
}
