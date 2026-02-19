package com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.response.GrammarRuleResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarExplanationParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarRule;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarScenarioSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.repo.GrammarRuleRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GrammarRuleControllerTests {

    @Test
    @DisplayName("Fetch grammar rule API: returns explanation paragraphs in response payload")
    public void fetchGrammarRuleReturnsExplanationParagraphsInApiPayload() throws Exception {
        var grammarRuleId = "rule-1";
        var repo = new FakeGrammarRuleRepo();
        repo.save(sampleGrammarRule(grammarRuleId));
        GrammarRuleOrchestrationService service = new GrammarRuleOrchestrationService(repo);
        var controller = new GrammarRuleController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/grammar-rules/{grammarRuleId}/v1", grammarRuleId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.id").value(grammarRuleId))
                .andExpect(jsonPath("$.response.explanationParagraphs[0]")
                        .value("Use present tense for current actions."))
                .andExpect(jsonPath("$.response.explanationParagraphs[1]")
                        .value("Conjugation changes by subject pronoun."))
                .andExpect(jsonPath("$.response.scenario.title").value("Coffee Shop Greeting"));
    }

    @Test
    @DisplayName("Fetch grammar rules API: returns explanation paragraphs in list payload")
    public void fetchGrammarRulesReturnsExplanationParagraphsInApiPayload() throws Exception {
        var repo = new FakeGrammarRuleRepo();
        repo.save(sampleGrammarRule("rule-1"));
        GrammarRuleOrchestrationService service = new GrammarRuleOrchestrationService(repo);
        var controller = new GrammarRuleController(service);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/grammar-rules/v1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].id").value("rule-1"))
                .andExpect(jsonPath("$.response[0].explanationParagraphs[0]")
                        .value("Use present tense for current actions."))
                .andExpect(jsonPath("$.response[0].explanationParagraphs[1]")
                        .value("Conjugation changes by subject pronoun."));
    }

    private GrammarRule sampleGrammarRule(String ruleId) {
        return new GrammarRule(
                new GrammarRule.GrammarRuleId(ruleId),
                "Present Tense",
                List.of(
                        new GrammarExplanationParagraph(
                                new GrammarExplanationParagraph.GrammarExplanationParagraphId("p-1"),
                                "Use present tense for current actions.",
                                0
                        ),
                        new GrammarExplanationParagraph(
                                new GrammarExplanationParagraph.GrammarExplanationParagraphId("p-2"),
                                "Conjugation changes by subject pronoun.",
                                1
                        )
                ),
                new GrammarScenario(
                        new GrammarScenario.GrammarScenarioId("scenario-1"),
                        "Coffee Shop Greeting",
                        "Two learners greet each other and order coffee using present tense.",
                        "de",
                        "SYSTEM",
                        true,
                        List.of(
                                new GrammarScenarioSentence(
                                        new GrammarScenarioSentence.GrammarScenarioSentenceId("s-1"),
                                        "Ich bestelle einen Kaffee.",
                                        "I order a coffee.",
                                        0
                                )
                        )
                )
        );
    }

    private static class FakeGrammarRuleRepo implements GrammarRuleRepo {
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
    }
}
