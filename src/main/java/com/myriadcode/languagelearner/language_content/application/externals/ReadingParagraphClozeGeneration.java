package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record ReadingParagraphClozeGeneration(
        String clozeParagraph,
        List<Item> items
) {
    public record Item(
            String vocabSource,
            String hint,
            List<String> answerWords,
            String blankToken
    ) {
    }
}
