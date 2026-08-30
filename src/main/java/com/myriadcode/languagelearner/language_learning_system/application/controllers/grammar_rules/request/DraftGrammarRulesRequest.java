package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DraftGrammarRulesRequest(
        String userId,
        String level,
        @JsonProperty("admin_key") String adminKey
) {
    public DraftGrammarRulesRequest(String level, String adminKey) {
        this(null, level, adminKey);
    }
}
