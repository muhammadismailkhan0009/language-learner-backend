package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeValidationException;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReadingPracticeMcpToolsTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final ReadingPracticeService readingPracticeService = mock(ReadingPracticeService.class);
    private final ReadingPracticeMcpTools tools = new ReadingPracticeMcpTools(jobService, readingPracticeService);

    @Test
    void fetchReturnsExactPromptForAuthenticatedPendingJob() {
        when(readingPracticeService.prepareGenerationPrompt("user-1")).thenReturn("reading prompt");

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getReadingPracticeGeneration()).isEqualTo("reading prompt");
        }

        verify(jobService).require("user-1", READING_PRACTICE);
    }

    @Test
    void successfulStorePersistsThenDeletesJob() {
        var generated = new ReadingPracticeReadingContent(List.of());

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeReadingPracticeGeneration(generated);
            assertThat(result.stored()).isTrue();
            assertThat(result.validationErrors()).isEmpty();
        }

        var order = inOrder(jobService, readingPracticeService);
        order.verify(jobService).require("user-1", READING_PRACTICE);
        order.verify(readingPracticeService).storeGeneration("user-1", generated);
        order.verify(jobService).delete("user-1");
    }

    @Test
    void validationErrorsAreReturnedAndJobRemainsForRetry() {
        var generated = new ReadingPracticeReadingContent(List.of());
        doThrow(new ReadingPracticeValidationException(List.of("Expected 3 scenarios but received 0")))
                .when(readingPracticeService).storeGeneration("user-1", generated);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeReadingPracticeGeneration(generated);
            assertThat(result.stored()).isFalse();
            assertThat(result.validationErrors()).containsExactly("Expected 3 scenarios but received 0");
        }

        verify(jobService, never()).delete("user-1");
    }
}
