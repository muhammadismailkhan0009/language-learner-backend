package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyDetailSeed;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyCandidatePrompt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyCandidatePromptContractTests {
    @Test
    void candidatePromptRequestsSurfaceOnlyWithoutSourceProvenance() {
        var prompt = PromptsGenerator.vocabularyCandidateExtraction("Am Bahnhof wartet Lina.");
        var work = new VocabularyCandidatePrompt("request-1", prompt);

        assertThat(work.requestId()).isEqualTo("request-1");
        assertThat(work.prompt()).contains("Am Bahnhof wartet Lina.", "only one field: surface", "Do not return translations")
                .doesNotContain("request-1", "Scenario ID", "scenarioId", "paragraph");
    }

    @Test
    void detailPromptCarriesCandidateIdsAndExactSurfaces() {
        var prompt = PromptsGenerator.vocabularyDetailGeneration(List.of(
                new VocabularyDetailSeed("candidate-1", "Bahnhof")));

        assertThat(prompt).contains("candidateId=candidate-1", "surface=Bahnhof", "WORD or CHUNK", "example sentence")
                .doesNotContain("Scenario ID", "scenarioId", "paragraph");
    }
}
