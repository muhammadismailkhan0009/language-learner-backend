package com.myriadcode.languagelearner.flashcards_study.domain.repos;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;

import java.util.List;
import java.util.Optional;

public interface FlashCardRepo {

    List<FlashCardData> findDataByDeckId(DeckId deckId);

    /**
     * @deprecated Obsolete generic review API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific review APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    Optional<FlashCardReview> findReviewInfoByCard(FlashCardReview.FlashCardId id);

    /**
     * @deprecated Obsolete generic deck API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific fetch APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    List<FlashCardReview> findFlashCardsByDeckAndUser(DeckId deckId, String userId);


    /**
     * @deprecated Obsolete generic save API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific save APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    void saveFlashCardState(FlashCardReview review);

    /**
     * @deprecated Obsolete generic create API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific create APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    void createFlashCard(FlashCardReview flashCardReview);

    /**
     * @deprecated Obsolete generic lookup API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific lookup APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    Optional<FlashCardReview> getCardAgainstContentAndUser(ContentId contentId,
                                                           ContentRefType contentType,
                                                           UserId userId);

    /**
     * @deprecated Obsolete generic lookup API for migration period only.
     * Prefer explicit vocabulary/sentence/chunk-specific lookup APIs.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    Optional<FlashCardReview> getCardAgainstContentAndUserAndDirection(ContentId contentId,
                                                                       ContentRefType contentType,
                                                                       UserId userId,
                                                                       boolean isReversed);

    List<FlashCardReview> findVocabularyFlashCardsByUser(String userId);

    Optional<FlashCardReview> findVocabularyReviewInfoByCard(FlashCardReview.FlashCardId id);

    void saveVocabularyFlashCardState(FlashCardReview review);

    void createVocabularyFlashCard(FlashCardReview review);

    Optional<FlashCardReview> getVocabularyCardAgainstContentAndUserAndDirection(ContentId vocabularyId,
                                                                                  UserId userId,
                                                                                  boolean isReversed);
}
