package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingContent;
import com.myriadcode.languagelearner.language_content.application.services.reading_practice.ReadingPracticeLlmAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReadingGenerationLlmBoundaryTests {

    @Test
    @DisplayName("Reading LLM boundary forwards profile level and eligible grammar titles")
    void forwardsGenerationContext() {
        var llmPort = mock(LLMPort.class);
        var vocabulary = List.of(new ReadingPracticeVocabularySeed("gehen", "go"));
        var grammarTitles = List.of("Present tense", "Modal verbs");
        when(llmPort.generateReadingContent("Daily walk", vocabulary, LanguageLevel.A2, grammarTitles))
                .thenReturn(new ReadingContent(List.of(
                        new ReadingContent.Paragraph("Ich gehe heute.", List.of("Ich gehe heute."))
                )));

        var result = new ReadingPracticeLlmAdapter(llmPort)
                .generateReadingContent("Daily walk", vocabulary, LanguageLevel.A2, grammarTitles);

        assertThat(result.paragraphs()).hasSize(1);
        verify(llmPort).generateReadingContent("Daily walk", vocabulary, LanguageLevel.A2, grammarTitles);
    }

    @Test
    @DisplayName("Reading LLM boundary avoids provider call when vocabulary is empty")
    void avoidsProviderCallForEmptyVocabulary() {
        var llmPort = mock(LLMPort.class);

        var result = new ReadingPracticeLlmAdapter(llmPort)
                .generateReadingContent("Daily walk", List.of(), LanguageLevel.A2, List.of("Modal verbs"));

        assertThat(result.paragraphs()).isEmpty();
        verifyNoInteractions(llmPort);
    }
}
