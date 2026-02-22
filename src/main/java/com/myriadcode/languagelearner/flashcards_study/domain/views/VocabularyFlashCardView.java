package com.myriadcode.languagelearner.flashcards_study.domain.views;

import java.util.List;

public record VocabularyFlashCardView(
        String id,
        Front front,
        Back back,
        boolean isReversed,
        boolean isRevision
) {
    public record Front(String wordOrChunk) {
    }

    public record Back(String wordOrChunk, List<Sentence> sentences) {
    }

    public record Sentence(String id, String sentence, String translation) {
    }
}
