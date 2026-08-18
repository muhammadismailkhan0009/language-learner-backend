package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WritingGenerationContextServiceTests {

    @Test
    void buildsProfileLevelAndEligibleGrammarTitles() {
        var repo = new InMemoryGrammarRuleRepo(List.of(
                rule("a1", "Present Tense", "A1", "READY", true),
                rule("a2", "Modal Verbs", "A2", "READY", true),
                rule("duplicate", " Modal Verbs ", "A2", "READY", true),
                rule("high", "Relative Clauses", "B1", "READY", true),
                rule("inactive", "Past Tense", "A1", "READY", false),
                rule("draft", "Draft Rule", "A1", "DRAFT", true),
                rule("invalid", "Invalid Rule", "unknown", "READY", true)
        ));
        var service = new WritingGenerationContextService(userId -> LanguageLevel.A2, repo);

        var context = service.build("user-1");

        assertThat(context.learnerLevel()).isEqualTo(LanguageLevel.A2);
        assertThat(context.grammarRuleTitles()).containsExactly("Present Tense", "Modal Verbs");
    }

    private GrammarRule rule(String id, String name, String level, String status, boolean active) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(id), id, name, level, status, active, List.of(), null);
    }

    private static class InMemoryGrammarRuleRepo implements GrammarRuleRepo {
        private final List<GrammarRule> rules;

        private InMemoryGrammarRuleRepo(List<GrammarRule> rules) {
            this.rules = new ArrayList<>(rules);
        }

        @Override public GrammarRule save(GrammarRule rule) { rules.add(rule); return rule; }
        @Override public Optional<GrammarRule> findById(String id) { return rules.stream().filter(rule -> rule.id().id().equals(id)).findFirst(); }
        @Override public List<GrammarRule> findAll() { return List.copyOf(rules); }
        @Override public List<GrammarRule> findByStatus(String status) { return rules.stream().filter(rule -> status.equals(rule.status())).toList(); }
        @Override public void deleteById(String id) { rules.removeIf(rule -> rule.id().id().equals(id)); }
    }
}
