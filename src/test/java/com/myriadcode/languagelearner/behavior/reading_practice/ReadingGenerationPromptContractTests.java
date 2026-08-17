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
                "Daily walk",
                List.of(new ReadingPracticeVocabularySeed("gehen", "go")),
                LanguageLevel.A2,
                List.of("Present tense", "Modal verbs")
        );

        assertThat(prompt)
                .contains("CEFR Level: A2")
                .contains("- Present tense", "- Modal verbs")
                .contains("Choose only the grammar rules that suit")
                .contains("You do not need to use every eligible grammar rule");
    }

    @Test
    @DisplayName("Reading prompt remains level-aware without eligible grammar titles")
    void supportsEmptyGrammarCatalog() {
        var prompt = PromptsGenerator.readingContentParagraphs(
                "Daily walk",
                List.of(new ReadingPracticeVocabularySeed("gehen", "go")),
                LanguageLevel.A1,
                List.of()
        );

        assertThat(prompt).contains("CEFR Level: A1", "Eligible Grammar-Rule Titles:", "(none provided)");
    }
}
