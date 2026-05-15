package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.request;

public record CreateReadingParagraphClozeSessionRequest(
        String userId,
        Integer limit
) {
}
