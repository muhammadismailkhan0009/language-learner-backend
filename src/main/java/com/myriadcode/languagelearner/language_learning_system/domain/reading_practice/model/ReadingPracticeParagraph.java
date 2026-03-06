package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model;

import java.util.List;

public record ReadingPracticeParagraph(
        ReadingPracticeParagraphId id,
        String text,
        int position,
        List<ReadingPracticeSentence> sentences
) {
    public record ReadingPracticeParagraphId(String id) {
    }
}
