package com.myriadcode.languagelearner.language_learning_system.application.mappers.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WritingPracticeApiMapperTests {

    private static final WritingPracticeApiMapper MAPPER = WritingPracticeApiMapper.INSTANCE;

    @Test
    @DisplayName("Summary mapping: trims preview to 180 chars and counts vocab")
    void summaryPreviewAndCount() {
        var longText = "x".repeat(250);
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s1"),
                new UserId("user-1"),
                "topic",
                longText,
                "german",
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                null,
                List.of(),
                List.of(
                        new WritingVocabularyUsage(new WritingVocabularyUsage.WritingVocabularyUsageId("u1"), "c1", "v1"),
                        new WritingVocabularyUsage(new WritingVocabularyUsage.WritingVocabularyUsageId("u2"), "c2", "v2")
                )
        );

        var summary = MAPPER.toSummary(session);

        assertThat(summary.sessionId()).isEqualTo("s1");
        assertThat(summary.vocabCount()).isEqualTo(2);
        assertThat(summary.englishParagraphPreview()).hasSize(180);
        assertThat(summary.submitted()).isFalse();
    }

    @Test
    @DisplayName("Response mapping: preserves paragraphs and sentence pairs")
    void responseMappingPreservesFields() {
        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s2"),
                new UserId("user-2"),
                "topic-2",
                "English paragraph.",
                "Deutscher Absatz.",
                Instant.parse("2026-01-01T00:00:00Z"),
                "My answer",
                Instant.parse("2026-01-01T01:00:00Z"),
                List.of(new WritingSentencePair(
                        new WritingSentencePair.WritingSentencePairId("p-1"),
                        "English sentence.",
                        "Deutscher Satz.",
                        0
                )),
                List.of()
        );

        var flashcards = List.of(new WritingVocabularyFlashCardView(
                "f-1",
                new WritingVocabularyFlashCardView.Front("word"),
                new WritingVocabularyFlashCardView.Back("Wort", List.of()),
                true
        ));

        var response = MAPPER.toResponse(session, flashcards);

        assertThat(response.sessionId()).isEqualTo("s2");
        assertThat(response.topic()).isEqualTo("topic-2");
        assertThat(response.englishParagraph()).isEqualTo("English paragraph.");
        assertThat(response.germanParagraph()).isEqualTo("Deutscher Absatz.");
        assertThat(response.submittedAnswer()).isEqualTo("My answer");
        assertThat(response.submittedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
        assertThat(response.sentencePairs()).hasSize(1);
        assertThat(response.sentencePairs().getFirst().englishSentence()).isEqualTo("English sentence.");
        assertThat(response.sentencePairs().getFirst().germanSentence()).isEqualTo("Deutscher Satz.");
        assertThat(response.vocabFlashcards()).hasSize(1);
        assertThat(response.vocabFlashcards().getFirst().id()).isEqualTo("f-1");
    }
}
