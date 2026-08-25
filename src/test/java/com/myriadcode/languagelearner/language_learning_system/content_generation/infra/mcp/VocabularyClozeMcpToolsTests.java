package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeBatch;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyClozeGenerationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CLOZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VocabularyClozeMcpToolsTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final VocabularyClozeGenerationService clozeService = mock(VocabularyClozeGenerationService.class);
    private final VocabularyClozeMcpTools tools = new VocabularyClozeMcpTools(jobService, clozeService);

    @Test
    void fetchRequiresTypedJobForAuthenticatedUser() {
        when(clozeService.preparePrompt("user-1")).thenReturn("prompt");

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getVocabularyClozeGeneration()).isEqualTo("prompt");
        }

        verify(jobService).require("user-1", VOCABULARY_CLOZE);
    }

    @Test
    void successfulStoreDeletesJobAfterPersistence() {
        var batch = new VocabularyClozeBatch(List.of());
        var expected = new GenerateVocabularyClozeSentencesResponse(2);
        when(clozeService.store("user-1", batch)).thenReturn(expected);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.storeVocabularyClozeGeneration(batch)).isSameAs(expected);
        }

        var order = inOrder(jobService, clozeService);
        order.verify(jobService).require("user-1", VOCABULARY_CLOZE);
        order.verify(clozeService).store("user-1", batch);
        order.verify(jobService).delete("user-1");
    }
}
