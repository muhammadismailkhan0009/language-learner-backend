package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;

import java.util.List;

public record ReadingParagraphClozeStoreResponse(
        boolean stored,
        List<String> validationErrors,
        ReadingParagraphClozeSessionResponse session
) {
}
