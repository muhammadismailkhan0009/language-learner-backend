package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class WritingPracticeFreeWritingPersistenceTests {

    @Autowired private WritingPracticeRepo writingPracticeRepo;
    @Autowired private WritingPracticeSessionJpaRepo sessionJpaRepo;

    @AfterEach
    void tearDown() {
        sessionJpaRepo.deleteAll();
    }

    @Test
    void roundTripsFreeWritingFieldsAndKeepsNullLegacyValuesReadable() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("session-free-writing"),
                new UserId("user-1"), Instant.EPOCH,
                List.of(
                        scenario("scenario-new", 0, "Write about your day.", "Mein Tag war gut."),
                        scenario("scenario-legacy", 1, null, null)));

        writingPracticeRepo.save(session);

        var loaded = writingPracticeRepo.findByIdAndUserId("session-free-writing", "user-1").orElseThrow();
        assertThat(loaded.scenarios().get(0).freeWritingInstructions()).isEqualTo("Write about your day.");
        assertThat(loaded.scenarios().get(0).freeWritingText()).isEqualTo("Mein Tag war gut.");
        assertThat(loaded.scenarios().get(1).freeWritingInstructions()).isNull();
        assertThat(loaded.scenarios().get(1).freeWritingText()).isNull();
    }

    @Test
    void submissionUpdatePersistsFreeWritingTextWithTranslation() {
        writingPracticeRepo.save(new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("session-submission"),
                new UserId("user-1"), Instant.EPOCH,
                List.of(scenario("scenario-1", 0, "Write about your day.", null))));

        writingPracticeRepo.updateSubmission(
                "session-submission", "scenario-1", "user-1",
                "Meine Ubersetzung", "Mein freier Text", Instant.now(), null, null, null);

        var scenario = writingPracticeRepo.findByIdAndUserId("session-submission", "user-1")
                .orElseThrow().scenarios().getFirst();
        assertThat(scenario.submittedAnswer()).isEqualTo("Meine Ubersetzung");
        assertThat(scenario.freeWritingText()).isEqualTo("Mein freier Text");
        assertThat(scenario.submittedAt()).isNotNull();
    }

    private WritingPracticeScenario scenario(String id, int position, String instructions, String text) {
        return new WritingPracticeScenario(
                new WritingPracticeScenario.WritingPracticeScenarioId(id), position, "Topic " + position,
                "English", "Deutsch", instructions, text,
                null, null, null, null, null, List.of(), List.of());
    }
}
