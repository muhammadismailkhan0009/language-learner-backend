package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultilingualVocabularyCandidatePromptContractTests {

    @Test
    void detectsLanguageAndTranslatesNonGermanTextBeforeExtraction() {
        var prompt = PromptsGenerator.vocabularyCandidateExtraction("Lina waits for the train.");

        assertThat(prompt)
                .contains("detect the language", "not German", "natural German", "before extracting vocabulary")
                .contains("Lina waits for the train.");
    }

    @Test
    void extractsGermanTextDirectlyAndKeepsSurfaceOnlyOutput() {
        var prompt = PromptsGenerator.vocabularyCandidateExtraction("Lina wartet auf den Zug.");

        assertThat(prompt)
                .contains("already German", "extract vocabulary directly")
                .contains("canonical German word or chunk", "exactly one field", "surface")
                .doesNotContain("Return the translated paragraph");
    }
}

