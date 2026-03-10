package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
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

class WritingPracticeServiceOrchestratorTests {

    private WritingPracticeRepo writingPracticeRepo;
    private FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private FetchPrivateVocabularyApi privateVocabularyApi;
    private WritingPracticeLlmApi writingPracticeLlmApi;

    private WritingPracticeService service;

    @BeforeEach
    void setUp() {
        writingPracticeRepo = mock(WritingPracticeRepo.class);
        flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
        privateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        writingPracticeLlmApi = mock(WritingPracticeLlmApi.class);

        service = new WritingPracticeService(
                writingPracticeRepo,
                flashcardReviewsApi,
                privateVocabularyApi,
                writingPracticeLlmApi
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
        verify(writingPracticeLlmApi, never()).selectTopicForWriting(any(), any(), any());
        verify(writingPracticeRepo, never()).save(any());
    }

    @Test
    @DisplayName("createSession: fails when no valid vocabulary candidates can be built")
    void createSessionFailsWhenNoCandidates() {
        var onlyReversed = List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, false),
                new VocabularyFlashcardReviewRecord("f-2", "v-2", State.NEW, false)
        );

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(onlyReversed);
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1", "v-2"), "user-1"))
                .thenReturn(List.of(vocab("v-1"), vocab("v-2")));

        assertThatThrownBy(() -> service.createSession("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No vocabulary candidates found for writing practice");

        verify(writingPracticeLlmApi, never()).generateBilingualContent(any(), any(), any());
        verify(writingPracticeRepo, never()).save(any());
    }

    @Test
    @DisplayName("createSession: falls back to general topic when LLM returns no topic")
    void createSessionUsesGeneralTopicFallback() {
        var reviews = List.of(new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true));

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(reviews);
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1"), "user-1"))
                .thenReturn(List.of(vocab("v-1")));
        when(writingPracticeRepo.findRecentTopicsByUserId("user-1", 10)).thenReturn(List.of("Old topic"));
        when(writingPracticeLlmApi.selectTopicForWriting(any(), eq(List.of("Old topic")), eq("B1"))).thenReturn("");
        when(writingPracticeLlmApi.generateBilingualContent(eq("General writing practice"), any(), eq("B1")))
                .thenReturn(new WritingPracticeBilingualContent("English paragraph.", "Deutscher Absatz."));
        when(writingPracticeLlmApi.identifyUsedVocabulary(any(), eq("English paragraph."), eq("Deutscher Absatz.")))
                .thenReturn(List.of("surface-v-1"));
        when(writingPracticeLlmApi.splitIntoSentencePairs("English paragraph.", "Deutscher Absatz."))
                .thenReturn(List.of(new WritingPracticeSentencePairSeed("English paragraph.", "Deutscher Absatz.")));

        service.createSession("user-1");

        verify(writingPracticeLlmApi).selectTopicForWriting(any(), eq(List.of("Old topic")), eq("B1"));
        verify(writingPracticeLlmApi).generateBilingualContent(eq("General writing practice"), any(), eq("B1"));
        verify(writingPracticeLlmApi).identifyUsedVocabulary(any(), eq("English paragraph."), eq("Deutscher Absatz."));
        verify(writingPracticeRepo).save(any(WritingPracticeSession.class));
    }

    @Test
    @DisplayName("getSession: hydrates flashcards only for vocabulary records that exist")
    void getSessionHydratesOnlyExistingVocabulary() {
        var usage1 = new WritingVocabularyUsage(new WritingVocabularyUsage.WritingVocabularyUsageId("u-1"), "f-1", "v-1");
        var usage2 = new WritingVocabularyUsage(new WritingVocabularyUsage.WritingVocabularyUsageId("u-2"), "f-2", "v-2");

        var session = new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("s-1"),
                new com.myriadcode.languagelearner.common.ids.UserId("user-1"),
                "Travel",
                "English paragraph.",
                "Deutscher Absatz.",
                Instant.parse("2026-01-01T00:00:00Z"),
                "My answer",
                Instant.parse("2026-01-01T01:00:00Z"),
                List.of(),
                List.of(usage1, usage2)
        );

        when(writingPracticeRepo.findByIdAndUserId("s-1", "user-1")).thenReturn(Optional.of(session));
        when(privateVocabularyApi.getVocabularyRecords(List.of("v-1", "v-2"), "user-1"))
                .thenReturn(List.of(new PrivateVocabularyRecord(
                        "v-1",
                        "user-1",
                        "Wort",
                        "word",
                        "WORD",
                        null,
                        List.of(new PrivateVocabularyRecord.ExampleSentenceRecord("ex-1", "Ich lerne.", "I learn.")),
                        null,
                        Instant.parse("2026-01-01T00:00:00Z")
                )));

        var response = service.getSession("user-1", "s-1");

        assertThat(response.sessionId()).isEqualTo("s-1");
        assertThat(response.vocabFlashcards()).hasSize(1);
        assertThat(response.vocabFlashcards().getFirst().id()).isEqualTo("f-1");
        assertThat(response.vocabFlashcards().getFirst().front().wordOrChunk()).isEqualTo("word");
        assertThat(response.vocabFlashcards().getFirst().back().wordOrChunk()).isEqualTo("Wort");
        assertThat(response.vocabFlashcards().getFirst().isReversed()).isTrue();
        assertThat(response.submittedAnswer()).isEqualTo("My answer");
        assertThat(response.submittedAt()).isEqualTo(Instant.parse("2026-01-01T01:00:00Z"));
    }

    @Test
    @DisplayName("getSession: throws when repository has no session for user")
    void getSessionThrowsWhenMissing() {
        when(writingPracticeRepo.findByIdAndUserId("s-404", "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSession("user-1", "s-404"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Writing session not found");

        verify(privateVocabularyApi, never()).getVocabularyRecords(any(), any());
    }

    @Test
    @DisplayName("detachFlashcard: forwards user/session/flashcard to repo")
    void detachFlashcardForwardsToRepo() {
        service.detachFlashcard("user-1", "session-1", "flashcard-1");

        verify(writingPracticeRepo).detachFlashcard("user-1", "session-1", "flashcard-1");
    }

    @Test
    @DisplayName("submitAnswer: trims input and forwards submission update to repo")
    void submitAnswerForwardsTrimmedValue() {
        service.submitAnswer("user-1", "session-1", "  My answer  ");

        verify(writingPracticeRepo).updateSubmission(eq("session-1"), eq("user-1"), eq("My answer"), any());
    }

    @Test
    @DisplayName("submitAnswer: rejects blank answer")
    void submitAnswerRejectsBlankAnswer() {
        assertThatThrownBy(() -> service.submitAnswer("user-1", "session-1", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Submitted answer must not be blank");

        verify(writingPracticeRepo, never()).updateSubmission(any(), any(), any(), any());
    }

    private PrivateVocabularyRecord vocab(String id) {
        return new PrivateVocabularyRecord(
                id,
                "user-1",
                "surface-" + id,
                "translation-" + id,
                "WORD",
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
