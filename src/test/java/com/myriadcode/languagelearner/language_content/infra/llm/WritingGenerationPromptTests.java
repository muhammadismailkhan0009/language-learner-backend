package com.myriadcode.languagelearner.language_content.infra.llm;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WritingGenerationPromptTests {

    @Test
    void bilingualPromptIncludesProfileLevelAndEligibleGrammarTitles() {
        var prompt = PromptsGenerator.writingBilingualContent(
                "At the market",
                List.of(new WritingPracticeVocabularySeed("kaufen", "to buy")),
                LanguageLevel.A2,
                List.of("Present Tense", "Modal Verbs")
        );

        assertThat(prompt).contains("CEFR Level: A2");
        assertThat(prompt).contains("- Present Tense", "- Modal Verbs");
        assertThat(prompt).contains("You do NOT need to use every eligible grammar rule");
        assertThat(prompt).contains("Never distort either paragraph merely to demonstrate a grammar rule");
        assertThat(prompt).contains("`usedVocabulary`");
        assertThat(prompt).contains("Include items not supplied as generation targets");
        assertThat(prompt).contains("canonical German vocabulary surface where possible");
        assertThat(prompt).contains("Unmatched surfaces are allowed and will be ignored by the backend");
        assertThat(prompt).contains("kaufen - to buy");
        assertThat(prompt).doesNotContain("stable identifier");
    }
}
