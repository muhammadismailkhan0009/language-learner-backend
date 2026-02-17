package com.myriadcode.languagelearner.language_learning_system.domain.scenarios.domain.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.util.List;

public record Scenario(ScenarioId id, UserId userId, String nature, String targetLanguage,
                       List<ScenarioSentence> sentences) {

    public record ScenarioId(String id) {
    }
}
