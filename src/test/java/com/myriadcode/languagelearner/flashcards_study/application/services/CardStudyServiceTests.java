package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.EventPublisher;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
