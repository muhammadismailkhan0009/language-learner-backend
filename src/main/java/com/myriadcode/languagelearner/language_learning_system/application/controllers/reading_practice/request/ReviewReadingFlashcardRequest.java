package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.request;

import com.myriadcode.fsrs.api.enums.Rating;

public record ReviewReadingFlashcardRequest(String userId, String scenarioId, Rating rating) {
}
