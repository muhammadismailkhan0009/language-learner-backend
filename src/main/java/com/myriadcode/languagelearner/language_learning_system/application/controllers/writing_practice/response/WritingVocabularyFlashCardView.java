package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response;

import java.util.List;

public record WritingVocabularyFlashCardView(
        String id,
        Front front,
        Back back,
        boolean isReversed
) {
    public record Front(String wordOrChunk) {
    }

    public record Back(String wordOrChunk, List<Sentence> sentences) {
    }

    public record Sentence(String id, String sentence, String translation) {
    }
}
