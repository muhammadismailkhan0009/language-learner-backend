package com.myriadcode.languagelearner.language_content.application.ports;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

public record GrammarLevelReassignmentProposalPort(
        String grammarRuleId,
        LanguageLevel currentLevel,
        LanguageLevel proposedLevel,
        boolean changeRequired,
        String reason
) {
}
