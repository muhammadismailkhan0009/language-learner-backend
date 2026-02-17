package com.myriadcode.languagelearner.language_learning_system.application.controllers.scenarios.request;

import java.util.List;

public record EditScenarioRequest(String nature, String targetLanguage, List<ScenarioSentenceUpdateRequest> sentences) {

    public record ScenarioSentenceUpdateRequest(String id, String sentence, String translation) {
    }
}
