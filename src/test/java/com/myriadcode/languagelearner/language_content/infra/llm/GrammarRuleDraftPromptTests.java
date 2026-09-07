package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
import com.myriadcode.languagelearner.language_learning_system.domain.grammar_rules.model.GrammarGenerationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrammarRuleDraftPromptTests {

    @Test
    @DisplayName("grammarRuleDrafts prompt includes global catalog context and non-duplication constraints")
    void grammarRuleDraftsPromptIncludesGlobalContext() {
        var prompt = PromptsGenerator.grammarRuleDrafts(
                "A2",
                "de",
                12,
                List.of(
                        new GrammarRuleCatalogContext("present-tense-basics", "Present Tense Basics", "A1"),
                        new GrammarRuleCatalogContext("relative-clauses", "Relative Clauses", "B1")
                )
        );

        assertThat(prompt).contains("full existing catalog across all levels");
        assertThat(prompt).contains("Do not return rules that overlap");
        assertThat(prompt).contains("identifier=present-tense-basics");
        assertThat(prompt).contains("identifier=relative-clauses");
        assertThat(prompt).contains("Requested level: A2");
    }

    @Test
    @DisplayName("MCP grammar-rule details prompt includes exact draft ID needed for storage")
    void grammarRuleDetailsPromptIncludesDraftId() {
        var prompt = new GrammarGenerationPromptAdapter().ruleDetailsPrompt(
                "B1",
                "de",
                List.of(new GrammarGenerationRequest.RuleSeed(
                        "draft-7f3a", "relative-clauses", "Relative Clauses"))
        );

        assertThat(prompt)
                .contains("Grammar draft ID: draft-7f3a")
                .contains("return this exact value as draftId")
                .contains("Identifier: relative-clauses")
                .contains("Rule name: Relative Clauses");
    }
}
