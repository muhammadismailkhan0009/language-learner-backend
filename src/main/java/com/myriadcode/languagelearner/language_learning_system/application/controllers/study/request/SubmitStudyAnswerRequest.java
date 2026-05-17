package com.myriadcode.languagelearner.language_learning_system.application.controllers.study.request;

public record SubmitStudyAnswerRequest(
        String userId,
        String answer
) {
}
