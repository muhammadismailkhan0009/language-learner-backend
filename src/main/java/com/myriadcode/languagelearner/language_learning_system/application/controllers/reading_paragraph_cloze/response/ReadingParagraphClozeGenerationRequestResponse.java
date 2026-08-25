package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response;

public record ReadingParagraphClozeGenerationRequestResponse(
        String message,
        ReadingParagraphClozeSessionResponse session
) {
}
