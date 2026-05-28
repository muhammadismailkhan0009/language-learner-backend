package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ApproveGrammarRulesRequest(
        List<ApprovedRule> rules,
        @JsonProperty("admin_key") String adminKey
) {
    public record ApprovedRule(String identifier,
                               String name,
                               String level,
                               Boolean active,
                               String targetLanguage,
                               java.util.List<String> explanationParagraphs,
                               java.util.List<Example> explanationExamples) {
    }

    public record Example(String sentence, String translation, String note) {
    }
}
