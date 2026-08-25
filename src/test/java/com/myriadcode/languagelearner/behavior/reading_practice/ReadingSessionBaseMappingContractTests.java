package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.mappers.ReadingPracticeJpaMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingSessionBaseMappingContractTests {

    @Test
    void baseMapperLeavesScenarioHierarchyForRepositoryAdapter() {
        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("session-1"),
                new UserId("user-1"),
                "Session topic",
                "Compatibility text",
                List.of(),
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(),
                List.of(new ReadingPracticeScenario(
                        new ReadingPracticeScenario.ReadingPracticeScenarioId("scenario-1"),
                        "Scenario topic",
                        "Scenario text",
                        0,
                        List.of(),
                        List.of()
                ))
        );

        var entity = ReadingPracticeJpaMapper.INSTANCE.toEntity(session);

        assertThat(entity.getId()).isEqualTo("session-1");
        assertThat(entity.getTopic()).isEqualTo("Session topic");
        assertThat(entity.getScenarios()).isEmpty();
    }
}
