package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingGenerationPromptContractTests {

    @Test
    @DisplayName("Reading prompt supplies level and grammar titles as optional natural choices")
    void suppliesLevelAndGrammarGuidance() {
        var prompt = PromptsGenerator.readingContentParagraphs(
                List.of(new ReadingPracticeVocabularySeed("gehen", "go")),
                List.of("Old walk"),
                LanguageLevel.A2,
                List.of("Present tense", "Modal verbs"),
                3
        );

        assertThat(prompt)
                .contains("CEFR level A2")
                .contains("- Present tense", "- Modal verbs")
                .contains("Prefer grammar rules from the supplied list when they fit naturally")
                .contains("Do not force every supplied grammar rule");
    }

    @Test
    @DisplayName("Reading prompt remains level-aware without eligible grammar titles")
    void supportsEmptyGrammarCatalog() {
        var prompt = PromptsGenerator.readingContentParagraphs(
                List.of(new ReadingPracticeVocabularySeed("gehen", "go")),
                List.of(),
                LanguageLevel.A1,
                List.of(),
                3
        );

        assertThat(prompt).contains("CEFR level A1", "Eligible Grammar-Rule Titles:", "(none provided)");
    }
}
