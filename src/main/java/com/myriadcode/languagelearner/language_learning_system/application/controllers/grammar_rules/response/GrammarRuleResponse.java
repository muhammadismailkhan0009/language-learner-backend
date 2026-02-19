package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response;

import java.util.List;

public record GrammarRuleResponse(String id, String name, List<String> explanationParagraphs,
                                  GrammarScenarioResponse scenario) {

    public record GrammarScenarioResponse(String id, String title, String description, String targetLanguage,
                                          boolean isFixed, List<GrammarScenarioSentenceResponse> sentences) {
    }

    public record GrammarScenarioSentenceResponse(String sentence, String translation) {
    }
}
