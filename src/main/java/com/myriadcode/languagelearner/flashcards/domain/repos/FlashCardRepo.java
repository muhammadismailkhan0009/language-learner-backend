package com.myriadcode.languagelearner.flashcards.domain.repos;

import com.myriadcode.languagelearner.flashcards.domain.models.DeckId;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardReview;

import java.util.List;
import java.util.Optional;

public interface FlashCardRepo {

    List<FlashCardData> findDataByDeckId(DeckId deckId);

    Optional<FlashCardReview> findReviewInfoByCard(FlashCardData.FlashCardId id);

    void saveFlashCardState(FlashCardReview review);
}
