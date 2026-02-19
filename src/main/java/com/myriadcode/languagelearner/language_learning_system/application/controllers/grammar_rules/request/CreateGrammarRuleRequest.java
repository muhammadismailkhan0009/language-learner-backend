package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateGrammarRuleRequest(String name, List<String> explanationParagraphs,
                                       GrammarScenarioRequest scenario,
                                       @JsonProperty("admin_key") String adminKey) {

    public record GrammarScenarioRequest(String title, String description, String targetLanguage,
                                         List<GrammarScenarioSentenceRequest> sentences) {
    }

    public record GrammarScenarioSentenceRequest(String sentence, String translation) {
    }
}
