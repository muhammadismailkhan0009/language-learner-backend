package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EditGrammarRuleRequest(String name, List<String> explanationParagraphs,
                                     GrammarScenarioUpdateRequest scenario,
                                     @JsonProperty("admin_key") String adminKey) {

    public record GrammarScenarioUpdateRequest(String title, String description, String targetLanguage,
                                               List<GrammarScenarioSentenceUpdateRequest> sentences) {
    }

    public record GrammarScenarioSentenceUpdateRequest(String sentence, String translation) {
    }
}
