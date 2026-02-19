package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

import java.util.List;

public record GrammarScenario(GrammarScenarioId id, String title, String description, String targetLanguage,
                              String createdBy, boolean isFixed, List<GrammarScenarioSentence> sentences) {

    public record GrammarScenarioId(String id) {
    }
}
