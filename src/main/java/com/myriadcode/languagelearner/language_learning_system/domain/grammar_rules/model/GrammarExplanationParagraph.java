package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model;

public record GrammarExplanationParagraph(GrammarExplanationParagraphId id, String text, int displayOrder) {

    public record GrammarExplanationParagraphId(String id) {
    }
}
