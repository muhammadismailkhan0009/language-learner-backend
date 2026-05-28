package com.myriadcode.languagelearner.language_content.application.ports;

public record GrammarRuleDraftProposalPort(
        String identifier,
        String name,
        String level,
        String targetLanguage
) {
}
