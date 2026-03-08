package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.repos.WritingPracticeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class WritingPracticeJpaRepoBoundaryTests {

    @Autowired
    private WritingPracticeRepo writingPracticeRepo;

    @Autowired
    private WritingPracticeSessionJpaRepo writingPracticeSessionJpaRepo;

    @AfterEach
    void tearDown() {
        writingPracticeSessionJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("Repo boundary save: persists session and assigns createdAt for sentence and usage rows")
    void saveAssignsCreatedAt() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                "English paragraph",
                "Deutscher Absatz",
                null,
                null,
                null,
                List.of(new WritingSentencePair(
                        new WritingSentencePair.WritingSentencePairId("p1"),
                        "English sentence",
                        "Deutscher Satz",
                        0
                )),
                List.of(new WritingVocabularyUsage(
                        new WritingVocabularyUsage.WritingVocabularyUsageId(UUID.randomUUID().toString()),
                        "c1",
                        "v1"
                ))
        );

        writingPracticeRepo.save(session);

        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId("s1", "user-1").orElseThrow();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getSentencePairs()).hasSize(1);
        assertThat(persisted.getSentencePairs().iterator().next().getCreatedAt()).isNotNull();
        assertThat(persisted.getVocabularyUsages()).hasSize(1);
        assertThat(persisted.getVocabularyUsages().iterator().next().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Repo boundary updateSubmission: stores answer and timestamp on session")
    void updateSubmissionStoresAnswer() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s2"),
                new UserId("user-1"),
                "topic",
                "English paragraph",
                "Deutscher Absatz",
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                null,
                List.of(),
                List.of()
        );
        writingPracticeRepo.save(session);

        writingPracticeRepo.updateSubmission(
                "s2",
                "user-1",
                "Submitted answer",
                Instant.parse("2026-01-01T01:00:00Z")
        );

        var persisted = writingPracticeSessionJpaRepo.findByIdAndUserId("s2", "user-1").orElseThrow();
        assertThat(persisted.getSubmittedAnswer()).isEqualTo("Submitted answer");
        assertThat(persisted.getSubmittedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
    }
}
