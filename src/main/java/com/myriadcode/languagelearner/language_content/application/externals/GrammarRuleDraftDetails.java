package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record GrammarRuleDraftDetails(
        String identifier,
        String name,
        String level,
        String targetLanguage,
        List<String> explanationParagraphs,
        List<GrammarRuleExample> explanationExamples
) {
    public record GrammarRuleExample(String sentence, String translation, String note) {
    }
}
