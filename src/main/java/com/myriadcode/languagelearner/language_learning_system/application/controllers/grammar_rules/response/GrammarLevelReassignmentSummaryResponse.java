package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response;

import java.util.List;

public record GrammarLevelReassignmentSummaryResponse(
        int reviewedCount,
        int changedCount,
        int unchangedCount,
        List<ChangedRuleResponse> changedRules
) {
    public record ChangedRuleResponse(
            String id,
            String name,
            String previousLevel,
            String proposedLevel,
            String reason
    ) {
    }
}
