package com.myriadcode.languagelearner.flashcards_study.domain.models;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import jakarta.validation.constraints.NotNull;

public record FlashCardReview(
        FlashCardId id,
        @NotNull UserId userId,
        @NotNull ContentId contentId,
        ContentRefType contentType,
        FsrsRescheduleResult cardReviewData,
        boolean isReversed
) {

    public FlashCardReview(FlashCardId id,
                           @NotNull UserId userId,
                           @NotNull ContentId contentId,
                           ContentRefType contentType,
                           FsrsCard cardReviewData,
                           boolean isReversed) {
        this(id, userId, contentId, contentType, FsrsRescheduleResult.withCard(cardReviewData), isReversed);
    }

    public record FlashCardId(String id) {
    }
}
