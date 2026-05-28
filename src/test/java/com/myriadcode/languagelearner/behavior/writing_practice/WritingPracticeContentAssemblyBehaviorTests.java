package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WritingPracticeContentAssemblyBehaviorTests {

    private final WritingPracticeContentAssembler assembler = new WritingPracticeContentAssembler();

    @Test
    void findUsedVocabularySurfacesReturnsEmptySetOnNullResponse() {
        var llmApi = mock(WritingPracticeLlmApi.class);
        when(llmApi.identifyUsedVocabulary(List.of(), "en", "de")).thenReturn(null);

        var surfaces = assembler.findUsedVocabularySurfaces(llmApi, List.of(), "en", "de");

        assertThat(surfaces).isEmpty();
    }

    @Test
    void findUsedVocabularySurfacesNormalizesAndDeduplicatesValues() {
        var llmApi = mock(WritingPracticeLlmApi.class);
        when(llmApi.identifyUsedVocabulary(
                List.of(new WritingPracticeVocabularySeed("Haus", "house")),
                "en",
                "de"
        )).thenReturn(Arrays.asList(" Haus ", "haus", null, " "));

        var surfaces = assembler.findUsedVocabularySurfaces(
                llmApi,
                List.of(new WritingPracticeVocabularySeed("Haus", "house")),
                "en",
                "de"
        );

        assertThat(surfaces).containsExactly("haus");
    }

    @Test
    void buildSentencePairsFallsBackToSinglePairWhenSplitIsEmpty() {
        var pairs = assembler.buildSentencePairs(List.of(), "English paragraph.", "Deutscher Absatz.");

        assertThat(pairs).hasSize(1);
        assertThat(pairs.getFirst().englishSentence()).isEqualTo("English paragraph.");
        assertThat(pairs.getFirst().germanSentence()).isEqualTo("Deutscher Absatz.");
        assertThat(pairs.getFirst().position()).isEqualTo(0);
    }

    @Test
    void buildSentencePairsSanitizesAndPreservesOrdering() {
        var pairs = assembler.buildSentencePairs(
                List.of(
                        new WritingPracticeSentencePairSeed("  One. ", " Eins. "),
                        new WritingPracticeSentencePairSeed("Two.", "Zwei.")
                ),
                "ignored",
                "ignored"
        );

        assertThat(pairs).hasSize(2);
        assertThat(pairs.get(0).englishSentence()).isEqualTo("One.");
        assertThat(pairs.get(0).germanSentence()).isEqualTo("Eins.");
        assertThat(pairs.get(0).position()).isEqualTo(0);
        assertThat(pairs.get(1).position()).isEqualTo(1);
    }
}
