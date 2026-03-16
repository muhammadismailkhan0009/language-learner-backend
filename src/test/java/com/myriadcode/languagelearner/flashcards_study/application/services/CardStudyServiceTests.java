package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.EventPublisher;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.domain.views.VocabularyFlashCardView;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardStudyServiceTests {

    private final FsrsEngine fsrsEngine = FsrsEngine.createDefault();

    @Test
    @DisplayName("Study selection ignores reversed vocabulary cards without cloze sentences")
    void studySelectionIgnoresReversedCardsWithoutClozeSentences() {
        var flashCardRepo = mock(FlashCardRepo.class);
        var fetchLanguageContentApi = mock(FetchLanguageContentApi.class);
        var fetchPrivateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        var eventPublisher = mock(EventPublisher.class);
        var service = new CardStudyService(
                flashCardRepo,
                fetchLanguageContentApi,
                fetchPrivateVocabularyApi,
                eventPublisher,
                new VocabularyFlashcardCooldownWindow()
        );

        var withoutCloze = flashcard("card-1", "vocab-1", true);
        var withCloze = flashcard("card-2", "vocab-2", true);

        when(flashCardRepo.findVocabularyFlashCardsByUser("user-1"))
                .thenReturn(List.of(withoutCloze, withCloze));
        when(fetchPrivateVocabularyApi.getVocabularyRecords(List.of("vocab-1", "vocab-2"), "user-1"))
                .thenReturn(List.of(
                        vocabulary("vocab-1", null),
                        vocabulary("vocab-2", new PrivateVocabularyRecord.ClozeSentenceRecord(
                                "cloze-2",
                                "Ich ___ Deutsch.",
                                "learn",
                                "lerne",
                                List.of("lerne"),
                                "learn"
                        ))
                ));

        var cards = service.getNextPrivateVocabularyCardsToStudy("user-1", 1);

        assertThat(cards).hasSize(1);
        assertThat(cards.getFirst().id()).isEqualTo("card-2");
        assertThat(cards.getFirst().front().clozeText()).isEqualTo("Ich ___ Deutsch.");
    }

    @Test
    @DisplayName("Revision selection ignores reversed vocabulary cards without cloze sentences")
    void revisionSelectionIgnoresReversedCardsWithoutClozeSentences() {
        var flashCardRepo = mock(FlashCardRepo.class);
        var fetchLanguageContentApi = mock(FetchLanguageContentApi.class);
        var fetchPrivateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        var eventPublisher = mock(EventPublisher.class);
        var service = new CardStudyService(
                flashCardRepo,
                fetchLanguageContentApi,
                fetchPrivateVocabularyApi,
                eventPublisher,
                new VocabularyFlashcardCooldownWindow()
        );

        var withoutCloze = revisionFlashcard("card-1", "vocab-1", true);
        var withCloze = revisionFlashcard("card-2", "vocab-2", true);

        when(flashCardRepo.findVocabularyFlashCardsByUser("user-1"))
                .thenReturn(List.of(withoutCloze, withCloze));
        when(fetchPrivateVocabularyApi.getVocabularyRecords(List.of("vocab-1", "vocab-2"), "user-1"))
                .thenReturn(List.of(
                        vocabulary("vocab-1", null),
                        vocabulary("vocab-2", new PrivateVocabularyRecord.ClozeSentenceRecord(
                                "cloze-2",
                                "Wir ___ morgen.",
                                "come",
                                "kommen",
                                List.of("kommen"),
                                "come"
                        ))
                ));

        var cards = service.getPrivateVocabularyCardsForRevision("user-1", 1);

        assertThat(cards).hasSize(1);
        assertThat(cards.getFirst().id()).isEqualTo("card-2");
        assertThat(cards.getFirst().isRevision()).isTrue();
    }

    @Test
    @DisplayName("Study selection prefers overdue lower-stability cards over upcoming ones")
    void studySelectionPrefersOverdueLowerStabilityCards() {
        var flashCardRepo = mock(FlashCardRepo.class);
        var fetchLanguageContentApi = mock(FetchLanguageContentApi.class);
        var fetchPrivateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        var eventPublisher = mock(EventPublisher.class);
        var service = new CardStudyService(
                flashCardRepo,
                fetchLanguageContentApi,
                fetchPrivateVocabularyApi,
                eventPublisher,
                new VocabularyFlashcardCooldownWindow()
        );

        var now = Instant.now();
        var overdueWeak = studyFlashcard("card-1", "vocab-1", now.minusSeconds(1200), 2.0, 5.0, 0, now.minusSeconds(7200));
        var overdueStrong = studyFlashcard("card-2", "vocab-2", now.minusSeconds(1200), 7.0, 5.0, 0, now.minusSeconds(3600));
        var upcoming = studyFlashcard("card-3", "vocab-3", now.plusSeconds(600), 1.0, 8.0, 1, now.minusSeconds(10800));

        when(flashCardRepo.findVocabularyFlashCardsByUser("user-1"))
                .thenReturn(List.of(overdueWeak, overdueStrong, upcoming));
        when(fetchPrivateVocabularyApi.getVocabularyRecords(List.of("vocab-1", "vocab-2", "vocab-3"), "user-1"))
                .thenReturn(List.of(
                        vocabulary("vocab-1", cloze("cloze-1", "Ich ___ eins.", "one", "eins")),
                        vocabulary("vocab-2", cloze("cloze-2", "Ich ___ zwei.", "two", "zwei")),
                        vocabulary("vocab-3", cloze("cloze-3", "Ich ___ drei.", "three", "drei"))
                ));

        var cards = service.getNextPrivateVocabularyCardsToStudy("user-1", 2);

        assertThat(cards).extracting(VocabularyFlashCardView::id)
                .containsExactly("card-1", "card-2");
    }

    @Test
    @DisplayName("Vocabulary review appends the latest FSRS review log to existing history")
    void vocabularyReviewAppendsLatestFsrsReviewLogToExistingHistory() {
        var flashCardRepo = mock(FlashCardRepo.class);
        var fetchLanguageContentApi = mock(FetchLanguageContentApi.class);
        var fetchPrivateVocabularyApi = mock(FetchPrivateVocabularyApi.class);
        var eventPublisher = mock(EventPublisher.class);
        var service = new CardStudyService(
                flashCardRepo,
                fetchLanguageContentApi,
                fetchPrivateVocabularyApi,
                eventPublisher,
                new VocabularyFlashcardCooldownWindow()
        );

        var existingReview = new FlashCardReview(
                new FlashCardReview.FlashCardId("card-1"),
                new UserId("user-1"),
                new ContentId("vocab-1"),
                ContentRefType.VOCABULARY,
                new FsrsRescheduleResult(
                        FsrsCardMapper.toDomain(fsrsEngine.createEmptyCard(Instant.now().minusSeconds(60))),
                        List.of(new ReviewLog(
                                4.0,
                                Instant.parse("2026-03-11T13:00:00Z"),
                                2,
                                1,
                                1,
                                com.myriadcode.fsrs.api.enums.ReviewLogRating.GOOD,
                                Instant.parse("2026-03-11T12:00:00Z"),
                                3,
                                5.0,
                                State.REVIEW
                        ))
                ),
                true
        );

        when(flashCardRepo.findVocabularyReviewInfoByCard(new FlashCardReview.FlashCardId("card-1")))
                .thenReturn(java.util.Optional.of(existingReview));
        doNothing().when(flashCardRepo).saveVocabularyFlashCardState(any(FlashCardReview.class));

        service.reviewVocabularyStudiedCard("card-1", Rating.GOOD);

        var reviewCaptor = org.mockito.ArgumentCaptor.forClass(FlashCardReview.class);
        verify(flashCardRepo).saveVocabularyFlashCardState(reviewCaptor.capture());
        assertThat(reviewCaptor.getValue().cardReviewData().reviewLogs()).hasSize(2);
        assertThat(reviewCaptor.getValue().cardReviewData().reviewLogs().getFirst())
                .isEqualTo(existingReview.cardReviewData().reviewLogs().getFirst());
    }

    private FlashCardReview flashcard(String flashcardId, String vocabularyId, boolean isReversed) {
        return new FlashCardReview(
                new FlashCardReview.FlashCardId(flashcardId),
                new UserId("user-1"),
                new ContentId(vocabularyId),
                ContentRefType.VOCABULARY,
                FsrsCardMapper.toDomain(fsrsEngine.createEmptyCard(Instant.now().minusSeconds(60))),
                isReversed
        );
    }

    private FlashCardReview revisionFlashcard(String flashcardId, String vocabularyId, boolean isReversed) {
        var now = Instant.now();
        return new FlashCardReview(
                new FlashCardReview.FlashCardId(flashcardId),
                new UserId("user-1"),
                new ContentId(vocabularyId),
                ContentRefType.VOCABULARY,
                new FsrsCard(
                        5.0,
                        now.plusSeconds(3600),
                        0,
                        0,
                        now.minusSeconds(600),
                        1,
                        1,
                        1,
                        2.0,
                        State.LEARNING
                ),
                isReversed
        );
    }

    private FlashCardReview studyFlashcard(String flashcardId,
                                           String vocabularyId,
                                           Instant due,
                                           double stability,
                                           double difficulty,
                                           int lapses,
                                           Instant lastReview) {
        return new FlashCardReview(
                new FlashCardReview.FlashCardId(flashcardId),
                new UserId("user-1"),
                new ContentId(vocabularyId),
                ContentRefType.VOCABULARY,
                new FsrsCard(
                        difficulty,
                        due,
                        0,
                        lapses,
                        lastReview,
                        1,
                        1,
                        1,
                        stability,
                        State.REVIEW
                ),
                true
        );
    }

    private PrivateVocabularyRecord.ClozeSentenceRecord cloze(String id,
                                                              String text,
                                                              String answerTranslation,
                                                              String answerText) {
        return new PrivateVocabularyRecord.ClozeSentenceRecord(
                id,
                text,
                answerTranslation,
                answerText,
                List.of(answerText),
                answerTranslation
        );
    }

    private PrivateVocabularyRecord vocabulary(String id, PrivateVocabularyRecord.ClozeSentenceRecord clozeSentence) {
        return new PrivateVocabularyRecord(
                id,
                "user-1",
                "surface-" + id,
                "translation-" + id,
                "WORD",
                null,
                List.of(),
                clozeSentence,
                Instant.now().minusSeconds(60)
        );
    }
}
