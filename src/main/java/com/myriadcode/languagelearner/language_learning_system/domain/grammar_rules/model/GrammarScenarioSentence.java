package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

public record GrammarScenarioSentence(GrammarScenarioSentenceId id, String sentence, String translation, int displayOrder) {

    public record GrammarScenarioSentenceId(String id) {
    }
}
