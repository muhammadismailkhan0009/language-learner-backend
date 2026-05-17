package com.myriadcode.languagelearner.language_learning_system.application.controllers.study.request;

public record CreateStudySessionRequest(
        String userId,
        Integer limit
) {
}
