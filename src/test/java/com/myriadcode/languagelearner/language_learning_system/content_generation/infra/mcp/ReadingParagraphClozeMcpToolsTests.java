package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGenerationContext;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.READING_PARAGRAPH_CLOZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReadingParagraphClozeMcpToolsTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final ReadingParagraphClozeService clozeService = mock(ReadingParagraphClozeService.class);
    private final ReadingParagraphClozeMcpTools tools = new ReadingParagraphClozeMcpTools(jobService, clozeService);

    @Test
    void fetchReturnsExistingLlmInputObject() {
        var context = new ClozeParagraphGenerationContext(LanguageLevel.A2, List.of(), List.of());
        when(jobService.exists("user-1", READING_PARAGRAPH_CLOZE)).thenReturn(true);
        when(clozeService.prepareGeneration("user-1")).thenReturn(context);
        when(clozeService.buildGenerationPrompt(context)).thenReturn("same prompt");

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getReadingParagraphClozeGeneration()).isEqualTo("same prompt");
        }

        verify(jobService).exists("user-1", READING_PARAGRAPH_CLOZE);
    }

    @Test
    void successfulStoreDeletesJobAfterExistingPersistence() {
        var generated = new ClozeParagraphGeneration(List.of());
        var expected = new ReadingParagraphClozeSessionResponse("s1", "A2", Instant.EPOCH, List.of());
        when(clozeService.storeGeneration("user-1", generated)).thenReturn(expected);

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeReadingParagraphClozeGeneration(generated);
            assertThat(result.stored()).isTrue();
            assertThat(result.validationErrors()).isEmpty();
            assertThat(result.session()).isSameAs(expected);
        }

        var order = inOrder(jobService, clozeService);
        order.verify(jobService).require("user-1", READING_PARAGRAPH_CLOZE);
        order.verify(clozeService).storeGeneration("user-1", generated);
        order.verify(jobService).delete("user-1", READING_PARAGRAPH_CLOZE);
    }

    @Test
    void validationFailureIsReturnedAndKeepsPendingJob() {
        var generated = new ClozeParagraphGeneration(List.of());
        when(clozeService.storeGeneration("user-1", generated))
                .thenThrow(new IllegalArgumentException("paragraphs must contain at least one paragraph"));

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeReadingParagraphClozeGeneration(generated);
            assertThat(result.stored()).isFalse();
            assertThat(result.validationErrors()).containsExactly("paragraphs must contain at least one paragraph");
            assertThat(result.session()).isNull();
        }

        verify(jobService, never()).delete("user-1", READING_PARAGRAPH_CLOZE);
    }
}
