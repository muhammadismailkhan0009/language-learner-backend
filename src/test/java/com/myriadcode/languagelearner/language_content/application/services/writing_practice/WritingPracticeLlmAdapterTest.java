package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WritingPracticeLlmAdapterTest {

    @Mock
    private LLMPort llmPort;

    @Test
    void shouldNotCallLlmWhenSelectingTopicWithEmptyVocabulary() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);

        var topic = adapter.selectTopicForWriting(List.of(), List.of(), "B1");

        assertThat(topic).isNull();
        verifyNoInteractions(llmPort);
    }

    @Test
    void shouldNotCallLlmWhenGeneratingBilingualContentWithEmptyVocabulary() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);

        var content = adapter.generateBilingualContent("topic", List.of(), "B1");

        assertThat(content.englishParagraph()).isEmpty();
        assertThat(content.germanParagraph()).isEmpty();
        verifyNoInteractions(llmPort);
    }

    @Test
    void shouldNotCallLlmWhenIdentifyingUsedVocabularyWithEmptyVocabulary() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);

        var used = adapter.identifyUsedVocabulary(List.of(), "en", "de");

        assertThat(used).isEmpty();
        verifyNoInteractions(llmPort);
    }
}
