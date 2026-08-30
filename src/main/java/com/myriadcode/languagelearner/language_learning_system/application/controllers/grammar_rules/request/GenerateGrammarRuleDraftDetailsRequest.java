package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GenerateGrammarRuleDraftDetailsRequest(
        String userId,
        @JsonProperty("admin_key") String adminKey
) {
    public GenerateGrammarRuleDraftDetailsRequest(String adminKey) {
        this(null, adminKey);
    }
}
