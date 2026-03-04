package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadingPracticeServiceOrchestratorTests {

    private ReadingPracticeRepo readingPracticeRepo;
    private FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private FetchPrivateVocabularyApi privateVocabularyApi;
    private ReadingPracticeLlmApi readingPracticeLlmApi;

    private ReadingPracticeService service;

    @BeforeEach
    void setUp() {
        readingPracticeRepo = mock(ReadingPracticeRepo.class);
        flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        privateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);

        service = new ReadingPracticeService(
                readingPracticeRepo,
                flashcardReviewsApi,
                privateVocabularyApi,
                readingPracticeLlmApi
        );
    }

    @Test
    @DisplayName("createSession: fails fast when user has no flashcard reviews")
    void createSessionFailsWhenNoFlashcards() {
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of());

        assertThatThrownBy(() -> service.createSession("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No vocabulary flashcards found for user");

        verify(privateVocabularyApi, never()).getVocabularyRecords(any(), any());
        verify(readingPracticeLlmApi, never()).selectTopicForTextGeneration(any(), any());
        verify(readingPracticeRepo, never()).save(any());
    }

    @Test
    @DisplayName("createSession: fails when no valid vocabulary candidates can be built")
    void createSessionFailsWhenNoCandidates() {
        var onlyReversed = List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true),
                new VocabularyFlashcardReviewRecord("f-2", "v-2", State.NEW, true)
        );

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(onlyReversed);
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1", "v-2"), "user-1"))
                .thenReturn(List.of(vocab("v-1"), vocab("v-2")));

        assertThatThrownBy(() -> service.createSession("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No vocabulary candidates found for reading practice");

        verify(readingPracticeLlmApi, never()).generateReadingText(any(), any(), any());
        verify(readingPracticeRepo, never()).save(any());
    }

    @Test
    @DisplayName("createSession: falls back to general topic when LLM returns no topic")
    void createSessionUsesGeneralTopicFallback() {
        var reviews = List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, false)
        );

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(reviews);
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1"), "user-1"))
                .thenReturn(List.of(vocab("v-1")));
        when(readingPracticeLlmApi.selectTopicForTextGeneration(any(), eq("B1"))).thenReturn("");
        when(readingPracticeLlmApi.generateReadingText(eq("General practice"), any(), eq("B1")))
                .thenReturn("fallback reading");

        service.createSession("user-1");

        verify(readingPracticeLlmApi).selectTopicForTextGeneration(any(), eq("B1"));
        verify(readingPracticeLlmApi).generateReadingText(eq("General practice"), any(), eq("B1"));
        verify(readingPracticeRepo).save(any(ReadingPracticeSession.class));
    }

    @Test
    @DisplayName("getSession: hydrates flashcards only for vocabulary records that exist")
    void getSessionHydratesOnlyExistingVocabulary() {
        var usage1 = new ReadingVocabularyUsage(
                new ReadingVocabularyUsage.ReadingVocabularyUsageId("u-1"),
                "f-1",
                "v-1"
        );
        var usage2 = new ReadingVocabularyUsage(
                new ReadingVocabularyUsage.ReadingVocabularyUsageId("u-2"),
                "f-2",
                "v-2"
        );

        var session = new ReadingPracticeSession(
                new ReadingPracticeSession.ReadingPracticeSessionId("s-1"),
                new com.myriadcode.languagelearner.common.ids.UserId("user-1"),
                "Travel",
                "Reading text",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(usage1, usage2)
        );

        when(readingPracticeRepo.findByIdAndUserId("s-1", "user-1"))
                .thenReturn(Optional.of(session));
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1", "v-2"), "user-1"))
                .thenReturn(List.of(
                        new PrivateVocabularyRecord(
                                "v-1",
                                "user-1",
                                "Wort",
                                "word",
                                "WORD",
                                List.of(new PrivateVocabularyRecord.ExampleSentenceRecord("ex-1", "Ich lerne.", "I learn.")),
                                Instant.parse("2026-01-01T00:00:00Z")
                        )
                ));

        var response = service.getSession("user-1", "s-1");

        assertThat(response.sessionId()).isEqualTo("s-1");
        assertThat(response.vocabFlashcards()).hasSize(1);
        assertThat(response.vocabFlashcards().getFirst().id()).isEqualTo("f-1");
        assertThat(response.vocabFlashcards().getFirst().front().wordOrChunk()).isEqualTo("Wort");
        assertThat(response.vocabFlashcards().getFirst().back().wordOrChunk()).isEqualTo("word");
        assertThat(response.vocabFlashcards().getFirst().back().sentences()).hasSize(1);
        assertThat(response.vocabFlashcards().getFirst().isReversed()).isFalse();
    }

    @Test
    @DisplayName("getSession: throws when repository has no session for user")
    void getSessionThrowsWhenMissing() {
        when(readingPracticeRepo.findByIdAndUserId("s-404", "user-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSession("user-1", "s-404"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reading session not found");

        verify(privateVocabularyApi, never()).getVocabularyRecords(any(), any());
    }

    private PrivateVocabularyRecord vocab(String id) {
        return new PrivateVocabularyRecord(
                id,
                "user-1",
                "surface-" + id,
                "translation-" + id,
                "WORD",
                List.of(),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
