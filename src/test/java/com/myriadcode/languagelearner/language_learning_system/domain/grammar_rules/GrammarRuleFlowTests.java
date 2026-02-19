package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.CreateGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.grammar_rules.request.EditGrammarRuleRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarRuleOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.grammar_rules.repos.GrammarRuleEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
public class GrammarRuleFlowTests {

    @Autowired
    private GrammarRuleOrchestrationService grammarRuleOrchestrationService;

    @Autowired
    private GrammarRuleEntityJpaRepo grammarRuleEntityJpaRepo;

    @AfterEach
    public void tearDown() {
        grammarRuleEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("Store grammar rule flow: persists rule with explanation paragraphs and fixed shared scenario")
    public void storeGrammarRule() {
        var saved = grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Present Tense Basics",
                        List.of(
                                "Use present tense for current actions.",
                                "Conjugation changes by subject pronoun."
                        ),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Coffee Shop Greeting",
                                "Two learners greet each other and order coffee using present tense.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Ich bestelle einen Kaffee.",
                                                "I order a coffee."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        assertThat(saved.id()).isNotBlank();
        assertThat(saved.name()).isEqualTo("Present Tense Basics");
        assertThat(saved.explanationParagraphs()).hasSize(2);
        assertThat(saved.scenario().isFixed()).isTrue();
        assertThat(saved.scenario().sentences()).hasSize(1);
        assertThat(grammarRuleEntityJpaRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Edit grammar rule flow: updates name, explanation paragraphs, and scenario sentences")
    public void editGrammarRuleUpdatesNameExplanationAndScenario() {
        var saved = grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Articles",
                        List.of("Articles define nouns."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "At the market",
                                "Learner buys fruit.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Ich nehme zwei Aepfel.",
                                                "I take two apples."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        var updated = grammarRuleOrchestrationService.editGrammarRule(
                saved.id(),
                new EditGrammarRuleRequest(
                        "Definite Articles",
                        List.of(
                                "Der, die, das are definite articles in German.",
                                "Article choice depends on gender and case."
                        ),
                        new EditGrammarRuleRequest.GrammarScenarioUpdateRequest(
                                "At the bakery",
                                "Learner orders bread and asks questions.",
                                "de",
                                List.of(
                                        new EditGrammarRuleRequest.GrammarScenarioSentenceUpdateRequest(
                                                "Haben Sie frisches Brot?",
                                                "Do you have fresh bread?"
                                        ),
                                        new EditGrammarRuleRequest.GrammarScenarioSentenceUpdateRequest(
                                                "Ich nehme ein Vollkornbrot.",
                                                "I will take a whole-grain bread."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        assertThat(updated.name()).isEqualTo("Definite Articles");
        assertThat(updated.explanationParagraphs()).hasSize(2);
        assertThat(updated.scenario().title()).isEqualTo("At the bakery");
        assertThat(updated.scenario().isFixed()).isTrue();
        assertThat(updated.scenario().sentences()).hasSize(2);
    }

    @Test
    @DisplayName("Fetch grammar rules flow: returns all stored grammar rules for selection view")
    public void fetchGrammarRulesReturnsAllRules() {
        grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Past Tense",
                        List.of("Use simple past for completed actions."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Weekend recap",
                                "Learner talks about last weekend.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Am Wochenende war ich im Park.",
                                                "On the weekend I was in the park."
                                        )
                                )
                        ),
                        "112233"
                )
        );
        grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Future Tense",
                        List.of("Use future tense for planned actions."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Trip planning",
                                "Learner plans a city trip.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Morgen fahren wir nach Berlin.",
                                                "Tomorrow we are going to Berlin."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        var all = grammarRuleOrchestrationService.fetchGrammarRules();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(rule -> rule.name())
                .contains("Past Tense", "Future Tense");
        assertThat(all)
                .extracting(rule -> rule.explanationParagraphs())
                .allSatisfy(paragraphs -> assertThat(paragraphs).isNotEmpty());
        assertThat(all)
                .flatExtracting(rule -> rule.scenario().sentences())
                .isNotEmpty();
    }

    @Test
    @DisplayName("Fetch grammar rule flow: returns selected rule details including scenario sentence examples")
    public void fetchGrammarRuleReturnsSelectedRuleWithScenarioSentences() {
        var saved = grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Present Continuous",
                        List.of("Use this tense for ongoing actions."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Office check-in",
                                "Learners discuss what they are doing right now.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Ich arbeite gerade.",
                                                "I am working right now."
                                        ),
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Wir lernen zusammen.",
                                                "We are learning together."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        var fetched = grammarRuleOrchestrationService.fetchGrammarRule(saved.id());

        assertThat(fetched.id()).isEqualTo(saved.id());
        assertThat(fetched.name()).isEqualTo("Present Continuous");
        assertThat(fetched.scenario().sentences())
                .extracting(sentence -> sentence.sentence() + "|" + sentence.translation())
                .containsExactly(
                        "Ich arbeite gerade.|I am working right now.",
                        "Wir lernen zusammen.|We are learning together."
                );
    }

    @Test
    @DisplayName("Fetch grammar rule flow: returns complete payload including explanation paragraphs")
    public void fetchGrammarRuleReturnsCompletePayloadIncludingExplanationParagraphs() {
        var saved = grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Modal Verbs",
                        List.of(
                                "Modal verbs express ability, permission, or obligation.",
                                "In German, modal verbs usually send the main verb to the end."
                        ),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Planning after class",
                                "Two learners discuss what they can and must do later.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Ich kann heute Abend lernen.",
                                                "I can study this evening."
                                        ),
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Wir muessen morgen frueh aufstehen.",
                                                "We must get up early tomorrow."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        var fetched = grammarRuleOrchestrationService.fetchGrammarRule(saved.id());

        assertThat(fetched.id()).isEqualTo(saved.id());
        assertThat(fetched.name()).isEqualTo("Modal Verbs");
        assertThat(fetched.explanationParagraphs())
                .containsExactly(
                        "Modal verbs express ability, permission, or obligation.",
                        "In German, modal verbs usually send the main verb to the end."
                );
        assertThat(fetched.scenario().title()).isEqualTo("Planning after class");
        assertThat(fetched.scenario().description())
                .isEqualTo("Two learners discuss what they can and must do later.");
        assertThat(fetched.scenario().targetLanguage()).isEqualTo("de");
        assertThat(fetched.scenario().isFixed()).isTrue();
        assertThat(fetched.scenario().sentences())
                .extracting(sentence -> sentence.sentence() + "|" + sentence.translation())
                .containsExactly(
                        "Ich kann heute Abend lernen.|I can study this evening.",
                        "Wir muessen morgen frueh aufstehen.|We must get up early tomorrow."
                );
    }

    @Test
    @DisplayName("Store grammar rule validation flow: rejects create when explanation paragraphs are missing")
    public void createGrammarRuleFailsWhenExplanationParagraphsMissing() {
        assertThatThrownBy(() -> grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Nouns",
                        List.of(),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Classroom objects",
                                "Learner identifies classroom objects.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Das ist ein Tisch.",
                                                "That is a table."
                                        )
                                )
                        ),
                        "112233"
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one paragraph");
    }

    @Test
    @DisplayName("Store grammar rule validation flow: rejects create when scenario sentence list is missing")
    public void createGrammarRuleFailsWhenScenarioSentencesMissing() {
        assertThatThrownBy(() -> grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Pronouns",
                        List.of("Pronouns can replace nouns."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Class intro",
                                "Learner introduces classmates.",
                                "de",
                                List.of()
                        ),
                        "112233"
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one sentence");
    }

    @Test
    @DisplayName("Store grammar rule validation flow: rejects create when admin key is invalid")
    public void createGrammarRuleFailsWhenAdminKeyInvalid() {
        assertThatThrownBy(() -> grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Pronouns",
                        List.of("Pronouns can replace nouns."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "Class intro",
                                "Learner introduces classmates.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Das ist mein Buch.",
                                                "That is my book."
                                        )
                                )
                        ),
                        "wrong-key"
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid admin key");
    }

    @Test
    @DisplayName("Edit grammar rule validation flow: rejects update when admin key is invalid")
    public void editGrammarRuleFailsWhenAdminKeyInvalid() {
        var saved = grammarRuleOrchestrationService.createGrammarRule(
                new CreateGrammarRuleRequest(
                        "Articles",
                        List.of("Articles define nouns."),
                        new CreateGrammarRuleRequest.GrammarScenarioRequest(
                                "At the market",
                                "Learner buys fruit.",
                                "de",
                                List.of(
                                        new CreateGrammarRuleRequest.GrammarScenarioSentenceRequest(
                                                "Ich nehme zwei Aepfel.",
                                                "I take two apples."
                                        )
                                )
                        ),
                        "112233"
                )
        );

        assertThatThrownBy(() -> grammarRuleOrchestrationService.editGrammarRule(
                saved.id(),
                new EditGrammarRuleRequest(
                        "Changed Name",
                        List.of("Updated explanation."),
                        new EditGrammarRuleRequest.GrammarScenarioUpdateRequest(
                                "At the bakery",
                                "Learner orders bread and asks questions.",
                                "de",
                                List.of(
                                        new EditGrammarRuleRequest.GrammarScenarioSentenceUpdateRequest(
                                                "Haben Sie frisches Brot?",
                                                "Do you have fresh bread?"
                                        )
                                )
                        ),
                        "wrong-key"
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid admin key");
    }
}
