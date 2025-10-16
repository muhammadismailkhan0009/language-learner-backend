package com.myriadcode.languagelearner.flashcards.domain.models;

import io.github.openspacedrepetition.Card;

public record FlashCardReview(
        FlashCardData.FlashCardId id,
        Card cardReviewData
) {


}
