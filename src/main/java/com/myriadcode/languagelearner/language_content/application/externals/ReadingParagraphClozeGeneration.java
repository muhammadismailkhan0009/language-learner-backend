package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record ReadingParagraphClozeGeneration(
        List<Paragraph> paragraphs
) {
    public record Paragraph(
            String scenarioLabel,
            String clozeParagraph,
            List<Item> items
    ) {
    }

    public record Item(
            String vocabSource,
            String hint,
            List<String> answerWords,
            String blankToken
    ) {
    }
}
