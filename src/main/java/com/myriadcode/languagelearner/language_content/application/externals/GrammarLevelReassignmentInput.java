package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record GrammarLevelReassignmentInput(
        String id,
        String title,
        String currentLevel,
        List<String> explanationParagraphs,
        List<GrammarExample> examples
) {
    public record GrammarExample(String sentence, String translation) {
    }
}
