package com.myriadcode.languagelearner.language_learning_system.scenarios.application.controllers.request;

import java.util.List;

public record CreateScenarioRequest(String nature, String targetLanguage, List<ScenarioSentenceRequest> sentences) {

    public record ScenarioSentenceRequest(String sentence, String translation) {
    }
}
