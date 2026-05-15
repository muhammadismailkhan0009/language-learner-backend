package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeCard;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class ReadingParagraphClozeJpaRepoBoundaryTests {

    @Autowired
    private ReadingParagraphClozeRepo repo;

    @Autowired
    private ReadingParagraphClozeSessionJpaRepo sessionJpaRepo;

    @AfterEach
    void tearDown() {
        sessionJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("Repo boundary save: persists session and reference-only card rows")
    @Transactional
    void savePersistsReferenceOnlyRows() {
        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___ nach Hause.",
                null,
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        null
                ))
        );

        repo.save(session);

        var persisted = sessionJpaRepo.findByIdAndUserId("s-1", "user-1").orElseThrow();
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getCards()).hasSize(1);
        var card = persisted.getCards().iterator().next();
        assertThat(card.getFlashcardId()).isEqualTo("f-1");
        assertThat(card.getVocabularyId()).isEqualTo("v-1");
        assertThat(card.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Repo boundary query: findLatestByUserId returns newest session")
    void findLatestByUserIdReturnsNewestSession() {
        repo.save(new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic-1",
                "para-1",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        ));
        repo.save(new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-2"),
                new UserId("user-1"),
                "topic-2",
                "para-2",
                Instant.parse("2026-01-02T00:00:00Z"),
                List.of()
        ));

        var latest = repo.findLatestByUserId("user-1").orElseThrow();

        assertThat(latest.id().id()).isEqualTo("s-2");
        assertThat(latest.topic()).isEqualTo("topic-2");
    }
}
