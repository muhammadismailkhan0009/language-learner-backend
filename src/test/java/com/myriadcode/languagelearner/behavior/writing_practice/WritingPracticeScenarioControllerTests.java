package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.WritingPracticeController;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.request.CreateWritingPracticeSessionRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.request.SubmitWritingPracticeAnswerRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeGenerationRequestService;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WritingPracticeScenarioControllerTests {
    private final WritingPracticeService practiceService = mock(WritingPracticeService.class);
    private final WritingPracticeGenerationRequestService requestService = mock(WritingPracticeGenerationRequestService.class);
    private final WritingPracticeController controller = new WritingPracticeController(practiceService, requestService);

    @Test
    void createRequestsMcpJobAndReturnsAccepted() {
        when(requestService.request("user-1")).thenReturn("Run your MCP tool.");
        var response = controller.createSession(new CreateWritingPracticeSessionRequest("user-1"));
        assertThat(response.getStatusCode().value()).isEqualTo(202);
        verify(requestService).request("user-1");
        verifyNoInteractions(practiceService);
    }

    @Test
    void submissionTargetsOneScenario() {
        controller.submitAnswer("session-1", "scenario-2", true,
                new SubmitWritingPracticeAnswerRequest("user-1", "draft answer"));
        verify(practiceService).submitAnswer("user-1", "session-1", "scenario-2", "draft answer", true);
    }

    @Test
    void detachTargetsOneScenario() {
        controller.detachFlashcard("session-1", "scenario-2", "card-1", "user-1");
        verify(practiceService).detachFlashcard("user-1", "session-1", "scenario-2", "card-1");
    }
}
