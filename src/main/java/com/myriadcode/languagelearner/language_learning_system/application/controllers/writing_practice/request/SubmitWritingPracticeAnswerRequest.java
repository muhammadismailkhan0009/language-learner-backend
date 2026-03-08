package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.request;

public record SubmitWritingPracticeAnswerRequest(
        String userId,
        String submittedAnswer
) {
}
