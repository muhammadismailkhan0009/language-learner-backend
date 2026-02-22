package com.myriadcode.languagelearner.flashcards_study.domain.models.vocabulary;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;

import java.util.List;

public record VocabularyFlashCardData(
        FlashCardReview.FlashCardId id,
        String frontWordOrChunk,
        String backWordOrChunk,
        List<SentenceData> sentences,
        boolean isReversed
) {
    public record SentenceData(String id, String sentence, String translation) {
    }
}
