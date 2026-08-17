package com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileGrammarRuleQueryServiceTests {

    @Test
    void filtersForProfileAndPreservesRepositoryOrder() {
        var repo = new OrderedGrammarRuleRepo(List.of(
                rule("a1-first", "A1", "READY", true),
                rule("b2-hidden", "B2", "READY", true),
                rule("a2-second", "A2", "READY", true),
                rule("a1-inactive", "A1", "READY", false),
                rule("a1-draft", "A1", "DRAFT", true),
                rule("invalid", "unknown", "READY", true)
        ));
        var service = new ProfileGrammarRuleQueryService(repo, userId -> LanguageLevel.A2);

        var result = service.fetchGrammarRules("user-1");

        assertThat(result).extracting(rule -> rule.id())
                .containsExactly("a1-first", "a2-second");
    }

    @Test
    void filtersDraftsForProfileAndPreservesRepositoryOrder() {
        var repo = new OrderedGrammarRuleRepo(List.of(
                rule("a1-first", "A1", "DRAFT", false),
                rule("b1-hidden", "B1", "DRAFT", false),
                rule("a2-second", "A2", "DRAFT", false),
                rule("ready-hidden", "A1", "READY", true),
                rule("invalid-hidden", "unknown", "DRAFT", false)
        ));
        var service = new ProfileGrammarRuleQueryService(repo, userId -> LanguageLevel.A2);

        var result = service.fetchDraftGrammarRules("user-1");

        assertThat(result).extracting(rule -> rule.id())
                .containsExactly("a1-first", "a2-second");
    }

    private GrammarRule rule(String id, String level, String status, boolean active) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(id), id, id, level, status, active, List.of(), null);
    }

    private static class OrderedGrammarRuleRepo implements GrammarRuleRepo {
        private final List<GrammarRule> rules;

        private OrderedGrammarRuleRepo(List<GrammarRule> rules) {
            this.rules = new ArrayList<>(rules);
        }

        @Override public GrammarRule save(GrammarRule rule) { rules.add(rule); return rule; }
        @Override public Optional<GrammarRule> findById(String id) { return rules.stream().filter(rule -> rule.id().id().equals(id)).findFirst(); }
        @Override public List<GrammarRule> findAll() { return List.copyOf(rules); }
        @Override public List<GrammarRule> findByStatus(String status) { return rules.stream().filter(rule -> status.equals(rule.status())).toList(); }
        @Override public void deleteById(String id) { rules.removeIf(rule -> rule.id().id().equals(id)); }
    }
}
