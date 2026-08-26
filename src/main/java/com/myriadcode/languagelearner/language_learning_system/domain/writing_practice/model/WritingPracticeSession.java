package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record WritingPracticeSession(
        WritingPracticeSessionId id,
        UserId userId,
        Instant createdAt,
        List<WritingPracticeScenario> scenarios
) {
    public WritingPracticeSession {
        scenarios = scenarios == null ? List.of() : List.copyOf(scenarios);
    }

    public record WritingPracticeSessionId(String id) {
    }
}
