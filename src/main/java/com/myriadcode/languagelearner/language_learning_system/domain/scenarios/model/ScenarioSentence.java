package com.myriadcode.languagelearner.language_learning_system.domain.scenarios.model;

public record ScenarioSentence(ScenarioSentenceId id, String sentence, String translation) {

    public record ScenarioSentenceId(String id) {
    }
}
