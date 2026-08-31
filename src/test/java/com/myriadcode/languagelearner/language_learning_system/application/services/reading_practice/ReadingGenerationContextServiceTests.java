package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingGenerationContextServiceTests {

    @Test
    void uses_reading_level_for_prompt_and_unified_level_for_grammar_eligibility() {
        var repo = new InMemoryGrammarRuleRepo(List.of(
                rule("a2", "Modal Verbs", "A2"),
                rule("b1", "Relative Clauses", "B1")
        ));
        var service = new ReadingGenerationContextService(new UserDifficultyLevelApi() {
            @Override
            public LanguageLevel getDifficultyLevel(String userId) {
                return LanguageLevel.A2;
            }

            @Override
            public LanguageLevel getReadingDifficultyLevel(String userId) {
                return LanguageLevel.B1;
            }
        }, repo);

        var context = service.build("user-1");

        assertThat(context.learnerLevel()).isEqualTo(LanguageLevel.B1);
        assertThat(context.grammarRuleTitles()).containsExactly("Modal Verbs");
    }

    private GrammarRule rule(String id, String name, String level) {
        return new GrammarRule(new GrammarRule.GrammarRuleId(id), id, name, level, "READY", true, List.of(), null);
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
