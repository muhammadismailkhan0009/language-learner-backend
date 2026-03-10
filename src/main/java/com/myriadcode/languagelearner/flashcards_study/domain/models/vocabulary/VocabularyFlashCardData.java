package com.myriadcode.languagelearner.flashcards_study.domain.models.vocabulary;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;

import java.util.List;

public record VocabularyFlashCardData(
        FlashCardReview.FlashCardId id,
        ClozeSentenceData clozeSentence,
        String vocabularyNotes,
        boolean isReversed
) {
    public record ClozeSentenceData(
            String id,
            String clozeText,
            String hint,
            String answerText,
            List<String> answerWords,
            String answerTranslation
    ) {
    }
}
