package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response;

import java.util.List;

public record GrammarRuleDraftDetailsResponse(
        String identifier,
        String name,
        String level,
        String targetLanguage,
        List<String> explanationParagraphs,
        List<GrammarRuleResponse.GrammarExplanationExampleResponse> explanationExamples
) {
}
