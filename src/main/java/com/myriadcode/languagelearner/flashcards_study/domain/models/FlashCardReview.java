package com.myriadcode.languagelearner.flashcards_study.domain.models;

import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.UserId;
import jakarta.validation.constraints.NotNull;

public record FlashCardReview(
        FlashCardId id,
        @NotNull UserId userId,
        @NotNull ContentId contentId,
        ContentRefType contentType,
        Card cardReviewData,
        boolean isReversed
) {

    public record ContentId(String id) {
    }

    public record FlashCardId(String id) {
    }
}
