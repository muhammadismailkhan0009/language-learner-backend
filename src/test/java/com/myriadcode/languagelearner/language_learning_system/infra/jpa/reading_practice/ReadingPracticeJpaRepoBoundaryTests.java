package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.repos.ReadingPracticeSessionJpaRepo;
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
class ReadingPracticeJpaRepoBoundaryTests {

    @Autowired
    private ReadingPracticeRepo readingPracticeRepo;

    @Autowired
    private ReadingPracticeSessionJpaRepo readingPracticeSessionJpaRepo;

    @AfterEach
    void tearDown() {
        readingPracticeSessionJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("Repo boundary save: persists session and assigns createdAt for usage rows")
    void saveAssignsCreatedAt() {
        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                "reading text",
                List.of(new ReadingPracticeParagraph(
                        new ReadingPracticeParagraph.ReadingPracticeParagraphId("p1"),
                        "para",
                        0,
                        List.of(new ReadingPracticeSentence(
                                new ReadingPracticeSentence.ReadingPracticeSentenceId("s1"),
                                "sentence",
                                0
                        ))
                )),
                null,
                List.of(new ReadingVocabularyUsage(
                        new ReadingVocabularyUsage.ReadingVocabularyUsageId(UUID.randomUUID().toString()),
                        "c1",
                        "v1"
                ))
        );

        readingPracticeRepo.save(session);

        var persisted = readingPracticeSessionJpaRepo.findByIdAndUserId("s1", "user-1")
                .orElseThrow();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getVocabularyUsages()).hasSize(1);
        var firstUsage = persisted.getVocabularyUsages().iterator().next();
        assertThat(firstUsage.getCreatedAt()).isNotNull();
        assertThat(persisted.getParagraphs()).hasSize(1);
        assertThat(persisted.getParagraphs().get(0).getCreatedAt()).isNotNull();
        assertThat(persisted.getParagraphs().get(0).getSentences()).hasSize(1);
        assertThat(persisted.getParagraphs().get(0).getSentences().get(0).getCreatedAt()).isNotNull();
    }
}
