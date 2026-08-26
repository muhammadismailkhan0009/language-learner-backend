package com.myriadcode.languagelearner.language_learning_system.application.mappers.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WritingPracticeScenarioApiMapperTests {
    @Test
    void mapsEveryScenarioWithoutFlatteningParagraphState() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("session-1"), new UserId("user-1"), Instant.EPOCH,
                List.of(scenario("scenario-1", 0, "Topic one"), scenario("scenario-2", 1, "Topic two")));

        var response = WritingPracticeApiMapper.INSTANCE.toResponse(session, ignored -> List.of());

        assertThat(response.scenarios()).extracting(value -> value.scenarioId())
                .containsExactly("scenario-1", "scenario-2");
        assertThat(response.scenarios()).extracting(value -> value.topic())
                .containsExactly("Topic one", "Topic two");
    }

    private WritingPracticeScenario scenario(String id, int position, String topic) {
        return new WritingPracticeScenario(new WritingPracticeScenario.WritingPracticeScenarioId(id), position, topic,
                "English", "Deutsch", null, null, null, null, null, List.of(), List.of());
    }
}
