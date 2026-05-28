package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogContext;
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
}
