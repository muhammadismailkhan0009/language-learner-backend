package com.myriadcode.languagelearner.behavior.grammar_rules;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCurationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftDetails;
import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleDraftProposal;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.DraftGrammarRulesRequest;
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

    @TestConfiguration
    static class Config {
        @Bean(name = "grammarRuleCurationLlmAdapter")
        GrammarRuleCurationLlmApi grammarRuleCurationLlmAdapter() {
            return new CapturingGrammarRuleCurationLlmApi();
        }
    }

    static class CapturingGrammarRuleCurationLlmApi implements GrammarRuleCurationLlmApi {
        private final AtomicReference<List<GrammarRuleCatalogContext>> lastExistingRules = new AtomicReference<>(List.of());

        @Override
        public List<GrammarRuleDraftProposal> proposeRules(String level, String targetLanguage, int count, List<GrammarRuleCatalogContext> existingRules) {
            lastExistingRules.set(existingRules);
            return List.of(new GrammarRuleDraftProposal("a2-word-order", "A2 Word Order", "A2", "de"));
        }

        @Override
        public GrammarRuleDraftDetails generateRuleDetails(String identifier, String name, String level, String targetLanguage) {
            return new GrammarRuleDraftDetails(identifier, name, level, targetLanguage, List.of(), List.of());
        }
    }
}
