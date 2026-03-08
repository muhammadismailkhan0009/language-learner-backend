package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSessionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WritingPracticeJpaMapperTests {

    private static final WritingPracticeJpaMapper MAPPER = WritingPracticeJpaMapper.INSTANCE;

    @Test
    @DisplayName("JPA mapper toEntity: maps ids, user, topic, and bilingual paragraphs")
    void toEntityMapsFields() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                "English paragraph",
                "Deutscher Absatz",
                Instant.parse("2026-01-01T00:00:00Z"),
                "Submitted answer",
                Instant.parse("2026-01-01T01:00:00Z"),
                List.of(),
                List.of()
        );

        WritingPracticeSessionEntity entity = MAPPER.toEntity(session);

        assertThat(entity.getId()).isEqualTo("s1");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getTopic()).isEqualTo("topic");
        assertThat(entity.getEnglishParagraph()).isEqualTo("English paragraph");
        assertThat(entity.getGermanParagraph()).isEqualTo("Deutscher Absatz");
        assertThat(entity.getSubmittedAnswer()).isEqualTo("Submitted answer");
        assertThat(entity.getSubmittedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
    }

    @Test
    @DisplayName("JPA mapper toDomain: maps ids and bilingual paragraphs")
    void toDomainMapsFields() {
        var entity = new WritingPracticeSessionEntity();
        entity.setId("s2");
        entity.setUserId("user-2");
        entity.setTopic("topic-2");
        entity.setEnglishParagraph("English paragraph 2");
        entity.setGermanParagraph("Deutscher Absatz 2");
        entity.setSubmittedAnswer("My answer");
        entity.setSubmittedAt(Instant.parse("2026-01-01T01:00:00Z"));
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        var domain = MAPPER.toDomain(entity);

        assertThat(domain.id().id()).isEqualTo("s2");
        assertThat(domain.userId().id()).isEqualTo("user-2");
        assertThat(domain.topic()).isEqualTo("topic-2");
        assertThat(domain.englishParagraph()).isEqualTo("English paragraph 2");
        assertThat(domain.germanParagraph()).isEqualTo("Deutscher Absatz 2");
        assertThat(domain.submittedAnswer()).isEqualTo("My answer");
        assertThat(domain.submittedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
    }

    @Test
    @DisplayName("JPA mapper sentence pair mapping: preserves both languages and position")
    void toSentencePairEntityMapsFields() {
        var pair = new WritingSentencePair(
                new WritingSentencePair.WritingSentencePairId("p1"),
                "English sentence",
                "Deutscher Satz",
                2
        );

        var entity = MAPPER.toSentencePairEntity(pair);

        assertThat(entity.getId()).isEqualTo("p1");
        assertThat(entity.getEnglishSentence()).isEqualTo("English sentence");
        assertThat(entity.getGermanSentence()).isEqualTo("Deutscher Satz");
        assertThat(entity.getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("JPA mapper usage mapping: maps flashcard id and vocabulary id")
    void toUsageEntityMapsFields() {
        var usage = new WritingVocabularyUsage(
                new WritingVocabularyUsage.WritingVocabularyUsageId("u1"),
                "c1",
                "v1"
        );

        var entity = MAPPER.toUsageEntity(usage);

        assertThat(entity.getId()).isEqualTo("u1");
        assertThat(entity.getFlashcardId()).isEqualTo("c1");
        assertThat(entity.getVocabularyId()).isEqualTo("v1");
    }
}
