package com.myriadcode.languagelearner.language_content.application.services.reading_practice;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReadingPracticeLlmAdapterTest {

    @Mock
    private LLMPort llmPort;

    @Test
    void shouldNotCallLlmWhenSelectingTopicWithEmptyVocabulary() {
        var adapter = new ReadingPracticeLlmAdapter(llmPort);

        var topic = adapter.selectTopicForTextGeneration(List.of(), List.of(), "B1");

        assertThat(topic).isNull();
        verifyNoInteractions(llmPort);
    }

    @Test
    void shouldNotCallLlmWhenGeneratingContentWithEmptyVocabulary() {
        var adapter = new ReadingPracticeLlmAdapter(llmPort);

        var content = adapter.generateReadingContent("topic", List.of(), "B1");

        assertThat(content.paragraphs()).isEmpty();
        verifyNoInteractions(llmPort);
    }

    @Test
    void shouldNotCallLlmWhenIdentifyingUsedVocabularyWithEmptyVocabulary() {
        var adapter = new ReadingPracticeLlmAdapter(llmPort);

        var used = adapter.identifyUsedVocabulary(List.of(), "text");

        assertThat(used).isEmpty();
        verifyNoInteractions(llmPort);
    }
}
