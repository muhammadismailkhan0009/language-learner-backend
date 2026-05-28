package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenerateGrammarRuleDetailsRequest(
        String level,
        String targetLanguage,
        List<RuleSeed> rules,
        @JsonProperty("admin_key") String adminKey
) {
    public record RuleSeed(String identifier, String name) {
    }
}
