package com.myriadcode.languagelearner.flashcards_study.domain.models;

/**
 * @deprecated Obsolete generic flashcard content shape used during migration.
 * Prefer explicit per-capability view models and remove/refactor this later.
 */
@Deprecated(since = "2026-02-22", forRemoval = true)
public record FlashCardData(
        FlashCardReview.FlashCardId id,
        String frontText,
        String backText,
        boolean isReversed
) {

}
