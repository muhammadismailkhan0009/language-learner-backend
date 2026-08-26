package com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response;

import java.time.Instant;
import java.util.List;

public record WritingPracticeSessionResponse(
        String sessionId,
        List<WritingPracticeScenarioResponse> scenarios,
        Instant createdAt
) {
}
