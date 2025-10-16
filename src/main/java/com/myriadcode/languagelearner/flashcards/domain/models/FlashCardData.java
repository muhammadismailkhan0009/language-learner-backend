package com.myriadcode.languagelearner.flashcards.domain.models;

public record FlashCardData(
        FlashCardId id,
        String frontText,
        String backText,
        boolean isReversed
) {
    public record FlashCardId(String id) {
    }
}
