package com.myriadcode.languagelearner.language_content.application.services.vocabulary;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class VocabularyClozeLlmAdapterTest {

    @Mock
    private LLMPort llmPort;

    @Test
    void shouldNotCallLlmWhenGeneratingClozeWithEmptyVocabulary() {
        var adapter = new VocabularyClozeLlmAdapter(llmPort);

        var result = adapter.generateClozeSentences("topic", List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(llmPort);
    }
}
