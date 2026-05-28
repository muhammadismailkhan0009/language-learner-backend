package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response;

public record GrammarRuleDraftResponse(
        String id,
        String identifier,
        String name,
        String level,
        String targetLanguage
) {
}
