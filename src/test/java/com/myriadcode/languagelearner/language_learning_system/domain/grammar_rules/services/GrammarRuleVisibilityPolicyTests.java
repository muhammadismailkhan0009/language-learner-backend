package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarRuleVisibilityPolicyTests {

    @Test
    void showsOnlyActiveNonDraftRulesAtOrBelowProfileLevel() {
        assertThat(visible(rule("A1", "READY", true), LanguageLevel.B1)).isTrue();
        assertThat(visible(rule("B1", "READY", true), LanguageLevel.B1)).isTrue();
        assertThat(visible(rule("B2", "READY", true), LanguageLevel.B1)).isFalse();
        assertThat(visible(rule("A1", "READY", false), LanguageLevel.B1)).isFalse();
        assertThat(visible(rule("A1", "DRAFT", true), LanguageLevel.B1)).isFalse();
        assertThat(visible(rule("invalid", "READY", true), LanguageLevel.B1)).isFalse();
    }

    private boolean visible(GrammarRule rule, LanguageLevel level) {
        return GrammarRuleVisibilityPolicy.isVisibleTo(rule, level);
    }

    private GrammarRule rule(String level, String status, boolean active) {
        return new GrammarRule(
                new GrammarRule.GrammarRuleId(level + status + active),
                "rule", "Rule", level, status, active, List.of(), null
        );
    }
}
