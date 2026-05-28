package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response;

import java.util.List;

public record GrammarRuleResponse(String id,
                                  String identifier,
                                  String name,
                                  String level,
                                  String status,
                                  boolean active,
                                  List<String> explanationParagraphs,
                                  List<GrammarExplanationExampleResponse> explanationExamples) {

    public record GrammarExplanationExampleResponse(String sentence, String translation, String note) {
    }

}
