package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingPracticeJpaMapperTests {

    private static final ReadingPracticeJpaMapper MAPPER = ReadingPracticeJpaMapper.INSTANCE;

    @Test
    @DisplayName("JPA mapper toEntity: maps ids, user, topic, and reading text")
    void toEntityMapsFields() {
        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                "reading text",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        );

        ReadingPracticeSessionEntity entity = MAPPER.toEntity(session);

        assertThat(entity.getId()).isEqualTo("s1");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getTopic()).isEqualTo("topic");
        assertThat(entity.getReadingText()).isEqualTo("reading text");
    }

    @Test
    @DisplayName("JPA mapper toDomain: maps ids and reading text")
    void toDomainMapsFields() {
        var entity = new ReadingPracticeSessionEntity();
        entity.setId("s2");
        entity.setUserId("user-2");
        entity.setTopic("topic-2");
        entity.setReadingText("reading text 2");
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        var domain = MAPPER.toDomain(entity);

        assertThat(domain.id().id()).isEqualTo("s2");
        assertThat(domain.userId().id()).isEqualTo("user-2");
        assertThat(domain.topic()).isEqualTo("topic-2");
        assertThat(domain.readingText()).isEqualTo("reading text 2");
    }

    @Test
    @DisplayName("JPA mapper usage mapping: maps flashcard id and vocabulary id")
    void toUsageEntityMapsFields() {
        var usage = new ReadingVocabularyUsage(
                new ReadingVocabularyUsage.ReadingVocabularyUsageId("u1"),
                "c1",
                "v1"
        );

        var entity = MAPPER.toUsageEntity(usage);

        assertThat(entity.getId()).isEqualTo("u1");
        assertThat(entity.getFlashcardId()).isEqualTo("c1");
        assertThat(entity.getVocabularyId()).isEqualTo("v1");
    }
}
