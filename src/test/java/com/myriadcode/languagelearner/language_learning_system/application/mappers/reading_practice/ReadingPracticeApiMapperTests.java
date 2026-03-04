package com.myriadcode.languagelearner.language_learning_system.application.mappers.reading_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReadingPracticeApiMapperTests {

    private static final ReadingPracticeApiMapper MAPPER = ReadingPracticeApiMapper.INSTANCE;

    @Test
    @DisplayName("Summary mapping: trims preview to 180 chars and counts vocab")
    void summaryPreviewAndCount() {
        var longText = "x".repeat(250);
        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                longText,
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(
                        new ReadingVocabularyUsage(
                                new ReadingVocabularyUsage.ReadingVocabularyUsageId("u1"),
                                "c1",
                                "v1"
                        ),
                        new ReadingVocabularyUsage(
                                new ReadingVocabularyUsage.ReadingVocabularyUsageId("u2"),
                                "c2",
                                "v2"
                        )
                )
        );

        var summary = MAPPER.toSummary(session);

        assertThat(summary.sessionId()).isEqualTo("s1");
        assertThat(summary.vocabCount()).isEqualTo(2);
        assertThat(summary.readingTextPreview()).hasSize(180);
    }

    @Test
    @DisplayName("Response mapping: preserves ids and topic")
    void responseMappingPreservesFields() {
        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("s2"),
                new UserId("user-2"),
                "topic-2",
                "reading text",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of()
        );

        var response = MAPPER.toResponse(session);

        assertThat(response.sessionId()).isEqualTo("s2");
        assertThat(response.topic()).isEqualTo("topic-2");
        assertThat(response.readingText()).isEqualTo("reading text");
    }
}
