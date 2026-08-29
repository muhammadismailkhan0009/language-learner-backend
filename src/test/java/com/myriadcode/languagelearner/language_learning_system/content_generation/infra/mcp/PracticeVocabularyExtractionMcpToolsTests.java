package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.ExtractPracticeVocabularyResult;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyExtractionValidationException;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.PRACTICE_VOCABULARY_EXTRACTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PracticeVocabularyExtractionMcpToolsTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final PracticeVocabularyService vocabularyService = mock(PracticeVocabularyService.class);
    private final PracticeVocabularyExtractionMcpTools tools =
            new PracticeVocabularyExtractionMcpTools(jobService, vocabularyService);

    @Test
    void getReturnsEmptyWithoutTypedJob() {
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getPracticeVocabularyExtraction()).isEmpty();
        }
        verify(vocabularyService, never()).prepareExtractionPrompt(anyString());
    }

    @Test
    void successfulStorePersistsThenDeletesRequestAndJob() {
        var selection = new ReadingUsedVocabularySelection(List.of("gehen"));
        var result = new ExtractPracticeVocabularyResult(1, 0, List.of("gehen"), List.of("v-1"));
        when(vocabularyService.storeExtraction("user-1", selection)).thenReturn(result);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.storePracticeVocabularyExtraction(selection).result()).isEqualTo(result);
        }

        var order = inOrder(jobService, vocabularyService);
        order.verify(jobService).require("user-1", PRACTICE_VOCABULARY_EXTRACTION);
        order.verify(vocabularyService).storeExtraction("user-1", selection);
        order.verify(vocabularyService).deleteExtractionRequest("user-1");
        order.verify(jobService).delete("user-1", PRACTICE_VOCABULARY_EXTRACTION);
    }

    @Test
    void validationFailureReturnsErrorsAndKeepsRequestAndJob() {
        var selection = new ReadingUsedVocabularySelection(List.of("unknown"));
        doThrow(new PracticeVocabularyExtractionValidationException(List.of("Unknown vocabulary surfaces: [unknown]")))
                .when(vocabularyService).storeExtraction("user-1", selection);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var response = tools.storePracticeVocabularyExtraction(selection);
            assertThat(response.stored()).isFalse();
            assertThat(response.validationErrors()).containsExactly("Unknown vocabulary surfaces: [unknown]");
        }
        verify(vocabularyService, never()).deleteExtractionRequest(anyString());
        verify(jobService, never()).delete(anyString(), eq(PRACTICE_VOCABULARY_EXTRACTION));
    }
}
