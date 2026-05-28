package com.myriadcode.languagelearner.behavior.grammar_rules;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.DraftGrammarRulesRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrammarRuleDraftGenerationBehaviorTests {

    @Test
    @DisplayName("draftGrammarRules: sends global existing catalog context and returns normalized response")
    void draftGrammarRulesBuildsGlobalCatalogContext() {
        var repo = new InMemoryRepo();
        repo.save(rule("rule-1", "present-tense-basics", "Present Tense Basics", "A1"));
        repo.save(rule("rule-2", "subordinate-clauses", "Subordinate Clauses", "B1"));

        var fakeLlm = new CapturingCurationApi();
        var service = new GrammarRuleOrchestrationService(repo, fakeLlm);

        var result = service.draftGrammarRules(new DraftGrammarRulesRequest("a2", "112233"));

        assertThat(fakeLlm.lastLevel).isEqualTo("A2");
        assertThat(fakeLlm.lastTargetLanguage).isEqualTo("de");
        assertThat(fakeLlm.lastCount).isEqualTo(12);
        assertThat(fakeLlm.lastExistingRules).hasSize(2);
        assertThat(fakeLlm.lastExistingRules)
                .extracting(GrammarRuleCatalogContext::identifier)
                .containsExactlyInAnyOrder("present-tense-basics", "subordinate-clauses");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().identifier()).isEqualTo("new-rule");
        assertThat(result.getFirst().level()).isEqualTo("A2");
        assertThat(result.getFirst().targetLanguage()).isEqualTo("de");
    }

    @Test
    @DisplayName("draftGrammarRules: defaults level to A1 when missing")
    void draftGrammarRulesDefaultsLevel() {
        var service = new GrammarRuleOrchestrationService(new InMemoryRepo(), new CapturingCurationApi());
        var result = service.draftGrammarRules(new DraftGrammarRulesRequest(null, "112233"));
        assertThat(result.getFirst().level()).isEqualTo("A1");
    }

    @Test
    @DisplayName("draftGrammarRules: rejects invalid admin key")
    void draftGrammarRulesRejectsInvalidAdminKey() {
        var service = new GrammarRuleOrchestrationService(new InMemoryRepo(), new CapturingCurationApi());
        assertThatThrownBy(() -> service.draftGrammarRules(new DraftGrammarRulesRequest("A1", "bad-key")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid admin key");
    }

    private static GrammarRule rule(String id, String identifier, String name, String level) {
        return new GrammarRule(
                new GrammarRule.GrammarRuleId(id),
                identifier,
                name,
                level,
                "READY",
                true,
                List.of(new GrammarExplanationParagraph(
                        new GrammarExplanationParagraph.GrammarExplanationParagraphId("p-" + id),
                        "Explanation",
                        0
                )),
                new GrammarScenario(
                        new GrammarScenario.GrammarScenarioId("s-" + id),
                        "Explanation examples",
                        "Examples",
                        "de",
                        "SYSTEM",
                        true,
                        List.of()
                )
        );
    }

    private static final class CapturingCurationApi implements GrammarRuleCurationLlmApi {
        private String lastLevel;
        private String lastTargetLanguage;
        private int lastCount;
        private List<GrammarRuleCatalogContext> lastExistingRules = List.of();

        @Override
        public List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
            this.lastLevel = level;
            this.lastTargetLanguage = targetLanguage;
            this.lastCount = count;
            this.lastExistingRules = existingRules;
            return List.of(new GrammarRuleDraftProposal("", "New Rule", level, "de"));
        }

        @Override
        public GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
            return new GrammarRuleDraftDetails(identifier, name, level, targetLanguage, List.of(), List.of());
        }
    }

    private static final class InMemoryRepo implements GrammarRuleRepo {
        private final Map<String, GrammarRule> data = new HashMap<>();

        @Override
        public GrammarRule save(GrammarRule grammarRule) {
            data.put(grammarRule.id().id(), grammarRule);
            return grammarRule;
        }

        @Override
        public Optional<GrammarRule> findById(String grammarRuleId) {
            return Optional.ofNullable(data.get(grammarRuleId));
        }

        @Override
        public List<GrammarRule> findAll() {
            return data.values().stream().toList();
        }

        @Override
        public List<GrammarRule> findByStatus(String status) {
            return data.values().stream().filter(rule -> status.equals(rule.status())).toList();
        }
    }
}
