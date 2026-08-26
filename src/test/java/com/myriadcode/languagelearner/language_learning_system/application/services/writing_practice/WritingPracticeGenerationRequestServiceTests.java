package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import org.junit.jupiter.api.Test;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WRITING_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WritingPracticeGenerationRequestServiceTests {
    private final ContentGenerationJobService jobService = mock(ContentGenerationJobService.class);
    private final WritingPracticeGenerationRequestService service = new WritingPracticeGenerationRequestService(jobService);

    @Test
    void createsWritingJobAndTellsUserToRunMcp() {
        assertThat(service.request(" user-1 ")).contains("Run your MCP tool");
        verify(jobService).createOrReplace("user-1", WRITING_PRACTICE);
    }

    @Test
    void rejectsBlankUser() {
        assertThatThrownBy(() -> service.request(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId is required");
    }
}
