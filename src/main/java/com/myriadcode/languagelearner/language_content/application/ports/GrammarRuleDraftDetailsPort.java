package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record GrammarRuleDraftDetailsPort(
        String identifier,
        String name,
        String level,
        String targetLanguage,
        List<String> explanationParagraphs,
        List<GrammarRuleExamplePort> explanationExamples
) {
    public record GrammarRuleExamplePort(String sentence, String translation, String note) {
    }
}
