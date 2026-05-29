package com.myriadcode.languagelearner.behavior.grammar_rules;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.DraftGrammarRulesRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.GenerateGrammarRuleDraftDetailsRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos.GrammarRuleEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Import({TestDbConfigs.class, GrammarRuleDraftGenerationFeatureFlowTests.Config.class})
class GrammarRuleDraftGenerationFeatureFlowTests {

    @Autowired
    private GrammarRuleOrchestrationService grammarRuleOrchestrationService;

    @Autowired
    private GrammarRuleEntityJpaRepo grammarRuleEntityJpaRepo;

    @Autowired
    private GrammarRuleCurationLlmApi grammarRuleCurationLlmApi;

    @AfterEach
    void tearDown() {
        grammarRuleEntityJpaRepo.deleteAll();
        var capturing = (CapturingGrammarRuleCurationLlmApi) grammarRuleCurationLlmApi;
        capturing.nextProposals.set(List.of(new GrammarRuleDraftProposal("a2-word-order", "A2 Word Order", "A2", "de")));
    }

    @Test
    @DisplayName("feature flow: drafts use full persisted grammar catalog as LLM exclusion context")
    void draftsUseFullPersistedCatalogAsExclusionContext() {
        grammarRuleOrchestrationService.createGrammarRule(new CreateGrammarRuleRequest(
                "present-tense-basics",
                "Present Tense Basics",
                "A1",
                true,
                List.of("Use present tense."),
                new CreateGrammarRuleRequest.GrammarScenarioRequest(
                        "Explanation examples",
                        "Examples",
                        "de",
                        List.of(new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest("Ich lerne.", "I learn."))
                ),
                "112233"
        ));
        grammarRuleOrchestrationService.createGrammarRule(new CreateGrammarRuleRequest(
                "relative-clauses",
                "Relative Clauses",
                "B1",
                true,
                List.of("Use relative pronouns."),
                new CreateGrammarRuleRequest.GrammarScenarioRequest(
                        "Explanation examples",
                        "Examples",
                        "de",
                        List.of(new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest("Das ist der Mann, der hier wohnt.", "That is the man who lives here."))
                ),
                "112233"
        ));

        var drafts = grammarRuleOrchestrationService.draftGrammarRules(new DraftGrammarRulesRequest("a2", "112233"));

        assertThat(drafts).isNotEmpty();
        assertThat(((CapturingGrammarRuleCurationLlmApi) grammarRuleCurationLlmApi).lastExistingRules.get())
                .extracting(GrammarRuleCatalogContext::identifier)
                .containsExactlyInAnyOrder("present-tense-basics", "relative-clauses");
    }

    @Test
    @DisplayName("feature flow: generated drafts are persisted and retrievable from draft queue")
    void generatedDraftsArePersistedAndRetrievable() {
        var drafts = grammarRuleOrchestrationService.draftGrammarRules(new DraftGrammarRulesRequest("a2", "112233"));

        assertThat(drafts).hasSize(1);
        assertThat(drafts.getFirst().id()).isNotBlank();

        var persistedDrafts = grammarRuleOrchestrationService.fetchDraftGrammarRules("112233");
        assertThat(persistedDrafts).hasSize(1);
        assertThat(persistedDrafts.getFirst().id()).isEqualTo(drafts.getFirst().id());
        assertThat(persistedDrafts.getFirst().identifier()).isEqualTo("a2-word-order");
    }

    @Test
    @DisplayName("feature flow: per-draft details generation transitions rule out of draft queue")
    void draftDetailsGenerationTransitionsOutOfDraftQueue() {
        var capturing = (CapturingGrammarRuleCurationLlmApi) grammarRuleCurationLlmApi;
        capturing.nextProposals.set(List.of(new GrammarRuleDraftProposal("case-endings-a2", "Case Endings A2", "A2", "de")));
        capturing.nextDetails.set(new GrammarRuleDraftDetails(
                "case-endings-a2",
                "Case Endings A2",
                "A2",
                "de",
                List.of("Use case endings based on article and role in sentence."),
                List.of(new GrammarRuleDraftDetails.GrammarRuleExample(
                        "Ich sehe den Mann.",
                        "I see the man.",
                        "Akkusativ for direct object."
                ))
        ));

        var drafts = grammarRuleOrchestrationService.draftGrammarRules(new DraftGrammarRulesRequest("a2", "112233"));
        var draftId = drafts.getFirst().id();

        var details = grammarRuleOrchestrationService.generateDraftDetailsForDraftId(
                draftId,
                new GenerateGrammarRuleDraftDetailsRequest("112233")
        );
        assertThat(details.explanationParagraphs()).isNotEmpty();
        assertThat(details.explanationExamples()).isNotEmpty();

        var remainingDrafts = grammarRuleOrchestrationService.fetchDraftGrammarRules("112233");
        assertThat(remainingDrafts).isEmpty();

        var updatedRule = grammarRuleOrchestrationService.fetchGrammarRule(draftId);
        assertThat(updatedRule.status()).isEqualTo("READY");
        assertThat(updatedRule.identifier()).isEqualTo("case-endings-a2");
    }

    @Test
    @DisplayName("feature flow: delete explanation removes entire grammar rule aggregate")
    void deleteExplanationRemovesEntireRuleAggregate() {
        var created = grammarRuleOrchestrationService.createGrammarRule(new CreateGrammarRuleRequest(
                "present-tense-basics",
                "Present Tense Basics",
                "A1",
                true,
                List.of(
                        "Use present tense for routine actions.",
                        "Verb agrees with subject."
                ),
                new CreateGrammarRuleRequest.GrammarScenarioRequest(
                        "Explanation examples",
                        "Examples",
                        "de",
                        List.of(new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest("Ich lerne jeden Tag.", "I learn every day."))
                ),
                "112233"
        ));

        var deleted = grammarRuleOrchestrationService.deleteGrammarRuleExplanation(
                created.id(),
                new com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.DeleteGrammarRuleExplanationRequest("112233")
        );

        assertThat(deleted.id()).isEqualTo(created.id());
        assertThat(deleted.identifier()).isEqualTo(created.identifier());
        assertThat(deleted.name()).isEqualTo(created.name());
        assertThatThrownBy(() -> grammarRuleOrchestrationService.fetchGrammarRule(created.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Grammar rule not found");
    }

    @Test
    @DisplayName("feature flow: delete explanation rejects invalid admin key")
    void deleteExplanationRejectsInvalidAdminKey() {
        var created = grammarRuleOrchestrationService.createGrammarRule(new CreateGrammarRuleRequest(
                "present-tense-basics",
                "Present Tense Basics",
                "A1",
                true,
                List.of("Use present tense."),
                new CreateGrammarRuleRequest.GrammarScenarioRequest(
                        "Explanation examples",
                        "Examples",
                        "de",
                        List.of(new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest("Ich lerne.", "I learn."))
                ),
                "112233"
        ));

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                grammarRuleOrchestrationService.deleteGrammarRuleExplanation(
                        created.id(),
                        new com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.DeleteGrammarRuleExplanationRequest("wrong-key")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid admin key");
    }

    @TestConfiguration
    static class Config {
        @Bean(name = "grammarRuleCurationLlmAdapter")
        GrammarRuleCurationLlmApi grammarRuleCurationLlmAdapter() {
            return new CapturingGrammarRuleCurationLlmApi();
        }
    }

    static class CapturingGrammarRuleCurationLlmApi implements GrammarRuleCurationLlmApi {
        private final AtomicReference<List<GrammarRuleCatalogContext>> lastExistingRules = new AtomicReference<>(List.of());
        private final AtomicReference<List<GrammarRuleDraftProposal>> nextProposals =
                new AtomicReference<>(List.of(new GrammarRuleDraftProposal("a2-word-order", "A2 Word Order", "A2", "de")));
        private final AtomicReference<GrammarRuleDraftDetails> nextDetails =
                new AtomicReference<>(new GrammarRuleDraftDetails(
                        "a2-word-order",
                        "A2 Word Order",
                        "A2",
                        "de",
                        List.of("Put the finite verb in position two in main clauses."),
                        List.of(new GrammarRuleDraftDetails.GrammarRuleExample(
                                "Heute lerne ich Deutsch.",
                                "Today I learn German.",
                                "Verb remains second."
                        ))
                ));

        @Override
        public List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
            lastExistingRules.set(existingRules);
            return nextProposals.get();
        }

        @Override
        public GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
            return nextDetails.get();
        }
    }
}
