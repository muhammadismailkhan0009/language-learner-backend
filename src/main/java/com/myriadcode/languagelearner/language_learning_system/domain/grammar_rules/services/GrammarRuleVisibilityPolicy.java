package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;

public final class GrammarRuleVisibilityPolicy {

    private static final String STATUS_DRAFT = "DRAFT";

    private GrammarRuleVisibilityPolicy() {
    }

    public static boolean isVisibleTo(GrammarRule rule, LanguageLevel profileLevel) {
        if (rule == null || profileLevel == null || !rule.active()
                || STATUS_DRAFT.equalsIgnoreCase(rule.status())) {
            return false;
        }

        try {
            return LanguageLevel.from(rule.level()).ordinal() <= profileLevel.ordinal();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean isDraftVisibleTo(GrammarRule rule, LanguageLevel profileLevel) {
        if (rule == null || profileLevel == null || !STATUS_DRAFT.equalsIgnoreCase(rule.status())) {
            return false;
        }

        try {
            return LanguageLevel.from(rule.level()).ordinal() <= profileLevel.ordinal();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
