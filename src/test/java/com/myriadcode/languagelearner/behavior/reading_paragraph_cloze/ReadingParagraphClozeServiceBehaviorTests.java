package com.myriadcode.languagelearner.behavior.reading_paragraph_cloze;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeCard;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReadingParagraphClozeServiceBehaviorTests {

    private final ReadingParagraphClozeRepo repo = mock(ReadingParagraphClozeRepo.class);
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
    private final VocabularyRepo vocabularyRepo = mock(VocabularyRepo.class);
    private final FetchRecentReadingTopicsApi recentReadingTopicsApi = mock(FetchRecentReadingTopicsApi.class);
    private final FetchRecentWritingTopicsApi recentWritingTopicsApi = mock(FetchRecentWritingTopicsApi.class);
    private final ReadingPracticeLlmApi readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);
    private final ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi = mock(ReviewVocabularyFlashcardApi.class);

    private final ReadingParagraphClozeService service = new ReadingParagraphClozeService(
            repo,
            flashcardReviewsApi,
            vocabularyRepo,
            recentReadingTopicsApi,
            recentWritingTopicsApi,
            readingPracticeLlmApi,
            reviewVocabularyFlashcardApi
    );

    @Test
    @DisplayName("createSession: generates and persists a new session with card references from LLM vocabSource")
    void createSessionGeneratesAndPersistsReferences() {
        when(repo.findLatestByUserId("user-1")).thenReturn(Optional.empty());
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, true),
                new VocabularyFlashcardReviewRecord("f-2", "v-2", State.NEW, true)
        ));
        when(vocabularyRepo.findByIds(List.of("v-1", "v-2"))).thenReturn(List.of(
                vocab("v-1", "gehen", "to go"),
                vocab("v-2", "kennen", "to know")
        ));
        when(recentReadingTopicsApi.findRecentTopics("user-1", 3)).thenReturn(List.of("Travel"));
        when(recentWritingTopicsApi.findRecentTopics("user-1", 3)).thenReturn(List.of("Work"));
        when(readingPracticeLlmApi.generateReadingParagraphCloze(eq("Travel | Work"), any(), eq("B1")))
                .thenReturn(new ReadingParagraphClozeGeneration(
                        "Ich ___ nach Berlin und ___ Anna.",
                        List.of(
                                new ReadingParagraphClozeGeneration.Item("gehen", "go", List.of("gehe"), "___"),
                                new ReadingParagraphClozeGeneration.Item("kennen", "know", List.of("kenne"), "___")
                        )
                ));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createSession("user-1", 50);

        assertThat(response.topic()).isEqualTo("Travel | Work");
        assertThat(response.clozeParagraph()).contains("___");
        assertThat(response.cards()).hasSize(2);
        assertThat(response.cards()).extracting(value -> value.flashcardId())
                .containsExactlyInAnyOrder("f-1", "f-2");
    }

    @Test
    @DisplayName("rateCard: GOOD detaches card from reading paragraph cloze session and delegates review")
    void rateCardGoodDetachesCardAndDelegatesToReviewApi() {
        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___ .",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        Instant.parse("2026-01-01T00:00:01Z")
                ))
        );
        var detached = new ReadingParagraphClozeSession(
                session.id(),
                session.userId(),
                session.topic(),
                session.clozeParagraph(),
                session.createdAt(),
                List.of()
        );
        when(repo.findByIdAndUserId("s-1", "user-1")).thenReturn(Optional.of(session));
        when(repo.save(any())).thenReturn(detached);
        when(vocabularyRepo.findByIds(List.of())).thenReturn(List.of());
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, null, 0.9, 1, 1, 0, Instant.parse("2026-01-01T00:05:00Z"), true)
        ));

        var response = service.rateCard("s-1", "user-1", "f-1", Rating.GOOD);

        verify(reviewVocabularyFlashcardApi).reviewVocabularyFlashcard("f-1", Rating.GOOD);
        verify(repo).save(any());
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("rateCard: HARD keeps card attached while delegating review")
    void rateCardHardKeepsCardAttached() {
        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___ .",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        Instant.parse("2026-01-01T00:00:01Z")
                ))
        );
        when(repo.findByIdAndUserId("s-1", "user-1")).thenReturn(Optional.of(session));
        when(vocabularyRepo.findByIds(List.of("v-1"))).thenReturn(List.of(vocab("v-1", "gehen", "to go")));
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, null, 0.9, 1, 1, 0, Instant.parse("2026-01-01T00:05:00Z"), true)
        ));

        var response = service.rateCard("s-1", "user-1", "f-1", Rating.HARD);

        verify(reviewVocabularyFlashcardApi).reviewVocabularyFlashcard("f-1", Rating.HARD);
        verify(repo, never()).save(any());
        assertThat(response.cards()).hasSize(1);
    }

    @Test
    @DisplayName("getActiveSession: throws when latest session is completed")
    void getActiveSessionThrowsWhenCompleted() {
        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId("s-1"),
                new UserId("user-1"),
                "topic",
                "Ich ___.",
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId("c-1"),
                        "f-1",
                        "v-1",
                        Instant.parse("2026-01-01T00:00:01Z")
                ))
        );
        when(repo.findLatestByUserId("user-1")).thenReturn(Optional.of(session));
        when(vocabularyRepo.findByIds(List.of("v-1"))).thenReturn(List.of(vocab("v-1", "gehen", "to go")));
        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, null, 0.9, 1, 1, 0, Instant.parse("2026-01-01T00:05:00Z"), true)
        ));

        assertThatThrownBy(() -> service.getActiveSession("user-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No active reading paragraph cloze session found");
    }

    private Vocabulary vocab(String id, String surface, String translation) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId("user-1"),
                surface,
                translation,
                Vocabulary.EntryKind.WORD,
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
