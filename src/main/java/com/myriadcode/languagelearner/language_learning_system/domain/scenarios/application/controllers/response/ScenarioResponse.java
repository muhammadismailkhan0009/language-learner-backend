package com.myriadcode.languagelearner.language_learning_system.domain.scenarios.application.controllers.response;

import java.util.List;

public record ScenarioResponse(String id, String nature, String targetLanguage, List<ScenarioSentenceResponse> sentences) {

    public record ScenarioSentenceResponse(String id, String sentence, String translation) {
    }
}
