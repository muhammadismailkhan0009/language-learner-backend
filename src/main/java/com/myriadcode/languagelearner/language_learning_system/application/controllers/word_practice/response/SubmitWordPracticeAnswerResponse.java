package com.myriadcode.languagelearner.language_learning_system.application.controllers.word_practice.response;

public record SubmitWordPracticeAnswerResponse(
        boolean correct,
        String vocabularySurface
) {
}
