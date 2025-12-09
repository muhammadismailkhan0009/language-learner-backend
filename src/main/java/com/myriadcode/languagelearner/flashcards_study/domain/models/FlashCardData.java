package com.myriadcode.languagelearner.flashcards_study.domain.models;

public record FlashCardData(
        FlashCardReview.FlashCardId id,
        String frontText,
        String backText,
        boolean isReversed
) {

}
