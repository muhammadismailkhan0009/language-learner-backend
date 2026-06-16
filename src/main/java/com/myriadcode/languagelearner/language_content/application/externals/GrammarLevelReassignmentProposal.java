package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

public record GrammarLevelReassignmentProposal(
        String grammarRuleId,
        LanguageLevel currentLevel,
        LanguageLevel proposedLevel,
        boolean changeRequired,
        String reason
) {
}
