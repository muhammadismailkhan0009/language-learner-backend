package com.myriadcode.languagelearner.language_learning_system.application.externals;

import com.myriadcode.fsrs.api.enums.State;

public record VocabularyFlashcardReviewRecord(
        String flashcardId,
        String vocabularyId,
        State fsrsState,
        boolean isReversed
) {
}
