package com.myriadcode.languagelearner.flashcards_study.domain.views;

import java.util.List;

public record VocabularyFlashCardView(
        String id,
        VocabularyFlashCardFront front,
        VocabularyFlashCardBack back,
        boolean isReversed,
        boolean isRevision
) {
    public record VocabularyFlashCardFront(String clozeText, String hint) {
    }

    public record VocabularyFlashCardBack(List<String> answerWords, String answerText, String answerTranslation, String notes) {
    }
}
