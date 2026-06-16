package com.myriadcode.languagelearner.behavior.level_alignment;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentInput;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarLevelReassignmentProposal;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarContentAuthorizationService;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarLevelReassignmentService;
import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenarioSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrammarLevelReassignmentServiceBehaviorTests {

    @Test
    @DisplayName("reassignLevels: updates only changed levels and leaves omitted rules unchanged")
    void updatesOnlyChangedLevels() {
        var repo = new InMemoryGrammarRuleRepo();
        repo.save(rule("rule-1", "Perfekt with sein", "A2"));
        repo.save(rule("rule-2", "Modal verbs", "A2"));
        repo.save(rule("rule-3", "Passive with modal verbs", "A2"));
        var llm = new FakeCurationApi(List.of(
                new GrammarLevelReassignmentProposal("rule-1", LanguageLevel.A2, LanguageLevel.A1, true, "Introduced earlier."),
                new GrammarLevelReassignmentProposal("rule-2", LanguageLevel.A2, LanguageLevel.A2, false, "Already suitable.")
        ));

        var service = new GrammarLevelReassignmentService(repo, llm, new GrammarContentAuthorizationService());
        var summary = service.reassignLevels("user-1");

        assertThat(summary.reviewedCount()).isEqualTo(3);
        assertThat(summary.changedCount()).isEqualTo(1);
        assertThat(summary.unchangedCount()).isEqualTo(2);
        assertThat(repo.findById("rule-1").orElseThrow().level()).isEqualTo("A1");
        assertThat(repo.findById("rule-2").orElseThrow().level()).isEqualTo("A2");
        assertThat(repo.findById("rule-3").orElseThrow().level()).isEqualTo("A2");
        assertThat(repo.savedIds).containsExactlyInAnyOrder("rule-1", "rule-2", "rule-3", "rule-1");
        assertThat(llm.lastInput).extracting(GrammarLevelReassignmentInput::id)
                .containsExactlyInAnyOrder("rule-1", "rule-2", "rule-3");
    }

    @Test
    @DisplayName("reassignLevels: rejects unknown IDs, duplicate IDs, and unsupported levels before saving")
    void rejectsInvalidLlmResultsBeforeSaving() {
        var repo = new InMemoryGrammarRuleRepo();
        repo.save(rule("rule-1", "Perfekt with sein", "A2"));

        var unknown = new GrammarLevelReassignmentService(
                repo,
                new FakeCurationApi(List.of(new GrammarLevelReassignmentProposal("missing", LanguageLevel.A2, LanguageLevel.A1, true, "No match."))),
                new GrammarContentAuthorizationService()
        );
        assertThatThrownBy(() -> unknown.reassignLevels("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown grammar rule ID");

        var duplicate = new GrammarLevelReassignmentService(
                repo,
                new FakeCurationApi(List.of(
                        new GrammarLevelReassignmentProposal("rule-1", LanguageLevel.A2, LanguageLevel.A1, true, "Move down."),
                        new GrammarLevelReassignmentProposal("rule-1", LanguageLevel.A2, LanguageLevel.B1, true, "Move up.")
                )),
                new GrammarContentAuthorizationService()
        );
        assertThatThrownBy(() -> duplicate.reassignLevels("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate grammar rule ID");

        var missingLevel = new GrammarLevelReassignmentService(
                repo,
                new FakeCurationApi(List.of(new GrammarLevelReassignmentProposal("rule-1", LanguageLevel.A2, null, true, "Invalid."))),
                new GrammarContentAuthorizationService()
        );
        assertThatThrownBy(() -> missingLevel.reassignLevels("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Proposed language level is required");
        assertThat(repo.findById("rule-1").orElseThrow().level()).isEqualTo("A2");
    }

    private static GrammarRule rule(String id, String name, String level) {
        return new GrammarRule(
                new GrammarRule.GrammarRuleId(id),
                id,
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
                        List.of(new GrammarScenarioSentence(
                                new GrammarScenarioSentence.GrammarScenarioSentenceId("sent-" + id),
                                "Ich lerne.",
                                "I learn.",
                                0
                        ))
                )
        );
    }

    private static final class FakeCurationApi implements GrammarRuleCurationLlmApi {
        private final List<GrammarLevelReassignmentProposal> proposals;
        private List<GrammarLevelReassignmentInput> lastInput = List.of();

        private FakeCurationApi(List<GrammarLevelReassignmentProposal> proposals) {
            this.proposals = proposals;
        }

        @Override
        public List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
            return List.of();
        }

        @Override
        public GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
            return new GrammarRuleDraftDetails(identifier, name, level, targetLanguage, List.of(), List.of());
        }

        @Override
        public List<GrammarLevelReassignmentProposal> reassignGrammarLevels(List<GrammarLevelReassignmentInput> grammarRules) {
            lastInput = grammarRules;
            return proposals;
        }
    }

    private static final class InMemoryGrammarRuleRepo implements GrammarRuleRepo {
        private final Map<String, GrammarRule> data = new HashMap<>();
        private final java.util.ArrayList<String> savedIds = new java.util.ArrayList<>();

        @Override
        public GrammarRule save(GrammarRule grammarRule) {
            data.put(grammarRule.id().id(), grammarRule);
            savedIds.add(grammarRule.id().id());
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

        @Override
        public void deleteById(String grammarRuleId) {
            data.remove(grammarRuleId);
        }
    }
}
