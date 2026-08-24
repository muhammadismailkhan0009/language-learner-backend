package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model;

import java.util.List;

public record ReadingParagraphClozeParagraph(
        ReadingParagraphClozeParagraphId id,
        int paragraphIndex,
        String scenarioLabel,
        String clozeParagraph,
        List<ReadingParagraphClozeBlank> blanks
) {
    public ReadingParagraphClozeParagraph {
        blanks = blanks == null ? List.of() : List.copyOf(blanks);
    }
    public record ReadingParagraphClozeParagraphId(String id) {
    }
}
