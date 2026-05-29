package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeleteGrammarRuleExplanationRequest(
        @JsonProperty("admin_key") String adminKey
) {
}
