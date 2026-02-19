package com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules;

import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.services.GrammarRuleDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrammarRuleDomainServiceTests {

    @Test
    @DisplayName("Create grammar rule: sets SYSTEM-owned fixed scenario and normalizes scenario sentence order")
    public void createAssignsSystemFixedScenarioAndNormalizesSentenceOrder() {
        var grammarRule = GrammarRuleDomainService.create(
                "Present Tense",
                List.of("Used for current actions."),
                new GrammarRuleDomainService.GrammarScenarioCreateInput(
                        "Coffee Shop",
                        "Two people order coffee.",
                        "de",
                        List.of(
                                new GrammarRuleDomainService.GrammarScenarioSentenceInput(
                                        "Ich trinke Wasser.",
                                        "I drink water."
                                ),
                                new GrammarRuleDomainService.GrammarScenarioSentenceInput(
                                        "Du trinkst Saft.",
                                        "You drink juice."
                                )
                        )
                )
        );

        assertThat(grammarRule.id().id()).isNotBlank();
        assertThat(grammarRule.grammarScenario().createdBy()).isEqualTo("SYSTEM");
        assertThat(grammarRule.grammarScenario().isFixed()).isTrue();
        assertThat(grammarRule.grammarScenario().sentences()).hasSize(2);
        assertThat(grammarRule.grammarScenario().sentences().get(0).displayOrder()).isEqualTo(0);
        assertThat(grammarRule.grammarScenario().sentences().get(1).displayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Create grammar rule: fails when a scenario sentence translation is blank")
    public void createFailsWhenScenarioSentenceTranslationMissing() {
        assertThatThrownBy(() -> GrammarRuleDomainService.create(
                "Pronouns",
                List.of("Pronouns replace nouns."),
                new GrammarRuleDomainService.GrammarScenarioCreateInput(
                        "Classroom",
                        "Learners talk in class.",
                        "de",
                        List.of(new GrammarRuleDomainService.GrammarScenarioSentenceInput("Das ist mein Buch.", " "))
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("translation is required");
    }

    @Test
    @DisplayName("Edit grammar rule: keeps existing scenario sentences when patch does not provide sentences")
    public void editPreservesScenarioWhenPatchOmitsSentences() {
        var existing = GrammarRuleDomainService.create(
                "Articles",
                List.of("Articles define nouns."),
                new GrammarRuleDomainService.GrammarScenarioCreateInput(
                        "Bakery",
                        "Learner buys bread.",
                        "de",
                        List.of(
                                new GrammarRuleDomainService.GrammarScenarioSentenceInput(
                                        "Ich kaufe Brot.",
                                        "I buy bread."
                                )
                        )
                )
        );

        var edited = GrammarRuleDomainService.edit(
                existing,
                "Definite Articles",
                List.of("Der, die, das are definite articles."),
                new GrammarRuleDomainService.GrammarScenarioPatchInput(
                        "Bakery",
                        "Learner buys bread.",
                        "de",
                        null
                )
        );

        assertThat(edited.name()).isEqualTo("Definite Articles");
        assertThat(edited.grammarScenario().sentences()).hasSize(1);
        assertThat(edited.grammarScenario().sentences().get(0).sentence()).isEqualTo("Ich kaufe Brot.");
    }
}
