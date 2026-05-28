package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

import java.util.List;
import java.util.Locale;

public record GrammarRule(GrammarRuleId id,
                          String identifier,
                          String name,
                          String level,
                          boolean active,
                          List<GrammarExplanationParagraph> explanationParagraphs,
                          GrammarScenario grammarScenario) {

    public GrammarRule(GrammarRuleId id,
                       String name,
                       List<GrammarExplanationParagraph> explanationParagraphs,
                       GrammarScenario grammarScenario) {
        this(id, toIdentifier(name), name, "A1", true, explanationParagraphs, grammarScenario);
    }

    private static String toIdentifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return "grammar-rule";
        }
        return raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    public record GrammarRuleId(String id) {
    }
}
