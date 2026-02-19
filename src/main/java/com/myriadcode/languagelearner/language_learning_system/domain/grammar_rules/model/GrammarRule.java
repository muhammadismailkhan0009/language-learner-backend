package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

import java.util.List;

public record GrammarRule(GrammarRuleId id, String name, List<GrammarExplanationParagraph> explanationParagraphs,
                          GrammarScenario grammarScenario) {

    public record GrammarRuleId(String id) {
    }
}
