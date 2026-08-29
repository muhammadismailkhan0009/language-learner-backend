package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeGeneration;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeGenerationValidationException;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WRITING_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WritingPracticeMcpToolsTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final WritingPracticeService writingPracticeService = mock(WritingPracticeService.class);
    private final WritingPracticeMcpTools tools = new WritingPracticeMcpTools(jobService, writingPracticeService);

    @Test
    void fetchReturnsEmptyForDifferentOrMissingJobType() {
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getWritingPracticeGeneration()).isEmpty();
        }
        verify(writingPracticeService, never()).prepareGenerationPrompt(anyString());
    }

    @Test
    void fetchReturnsExactPromptForPendingWritingJob() {
        when(jobService.exists("user-1", WRITING_PRACTICE)).thenReturn(true);
        when(writingPracticeService.prepareGenerationPrompt("user-1")).thenReturn("writing prompt");
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getWritingPracticeGeneration()).isEqualTo("writing prompt");
        }
    }

    @Test
    void successfulStoreDeletesJobAfterPersistence() {
        var generated = new WritingPracticeGeneration(List.of());
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.storeWritingPracticeGeneration(generated).stored()).isTrue();
        }
        var order = inOrder(jobService, writingPracticeService);
        order.verify(jobService).require("user-1", WRITING_PRACTICE);
        order.verify(writingPracticeService).storeGeneration("user-1", generated);
        order.verify(jobService).delete("user-1", WRITING_PRACTICE);
    }

    @Test
    void validationFailureReturnsErrorsAndKeepsJob() {
        var generated = new WritingPracticeGeneration(List.of());
        doThrow(new WritingPracticeGenerationValidationException(List.of("scenarios must contain exactly 3 items")))
                .when(writingPracticeService).storeGeneration("user-1", generated);
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeWritingPracticeGeneration(generated);
            assertThat(result.stored()).isFalse();
            assertThat(result.validationErrors()).containsExactly("scenarios must contain exactly 3 items");
        }
        verify(jobService, never()).delete("user-1", WRITING_PRACTICE);
    }
}
