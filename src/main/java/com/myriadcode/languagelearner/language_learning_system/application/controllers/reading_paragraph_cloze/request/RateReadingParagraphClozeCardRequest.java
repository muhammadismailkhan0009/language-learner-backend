package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.request;

import com.myriadcode.fsrs.api.enums.Rating;

public record RateReadingParagraphClozeCardRequest(
        String userId,
        String flashcardId,
        Rating rating
) {
}
