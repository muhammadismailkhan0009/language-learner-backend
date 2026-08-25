package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;
import org.junit.jupiter.api.Test;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CLOZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VocabularyClozeGenerationRequestServiceTests {
    private final VocabularyClozeGenerationService generationService = mock(VocabularyClozeGenerationService.class);
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);

    @Test
    void mcpProviderCreatesJobWithoutCallingLlm() {
        var service = new VocabularyClozeGenerationRequestService(generationService, jobService, "MCP");

        var response = service.request("user-1");

        assertThat(response.generatedCount()).isZero();
        verify(jobService).createOrReplace("user-1", VOCABULARY_CLOZE);
        verify(generationService, never()).generate("user-1");
    }

    @Test
    void llmApiProviderUsesExistingGeneration() {
        var expected = new GenerateVocabularyClozeSentencesResponse(3);
        when(generationService.generate("user-1")).thenReturn(expected);
        var service = new VocabularyClozeGenerationRequestService(generationService, jobService, "LLM_API");

        assertThat(service.request("user-1")).isSameAs(expected);
        verify(jobService, never()).createOrReplace("user-1", VOCABULARY_CLOZE);
    }
}
