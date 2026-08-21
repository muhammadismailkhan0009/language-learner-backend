package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.WritingBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WritingPracticeLlmAdapterTest {

    @Mock
    private LLMPort llmPort;

    @Test
    void shouldNotCallLlmWhenSelectingTopicWithEmptyVocabulary() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);

        var topic = adapter.selectTopicForWriting(List.of(), List.of(), LanguageLevel.B1);

        assertThat(topic).isNull();
        verifyNoInteractions(llmPort);
    }

    @Test
    void shouldNotCallLlmWhenGeneratingBilingualContentWithEmptyVocabulary() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);

        var content = adapter.generateBilingualContent("topic", List.of(), LanguageLevel.B1, List.of());

        assertThat(content.englishParagraph()).isEmpty();
        assertThat(content.germanParagraph()).isEmpty();
        assertThat(content.usedVocabulary()).isEmpty();
        verifyNoInteractions(llmPort);
    }

    @Test
    void mapsUsedVocabularyFromBilingualGenerationResponse() {
        var adapter = new WritingPracticeLlmAdapter(llmPort);
        var vocabulary = List.of(new WritingPracticeVocabularySeed("gehen", "to go"));
        when(llmPort.generateWritingBilingualContent("topic", vocabulary, LanguageLevel.B1, List.of()))
                .thenReturn(new WritingBilingualContent("We go.", "Wir gehen.", List.of(" gehen ")));

        var content = adapter.generateBilingualContent("topic", vocabulary, LanguageLevel.B1, List.of());

        assertThat(content.englishParagraph()).isEqualTo("We go.");
        assertThat(content.germanParagraph()).isEqualTo("Wir gehen.");
        assertThat(content.usedVocabulary()).containsExactly("gehen");
    }
}
