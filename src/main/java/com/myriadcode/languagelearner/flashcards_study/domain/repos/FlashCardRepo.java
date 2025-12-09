package com.myriadcode.languagelearner.flashcards_study.domain.repos;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;

import java.util.List;
import java.util.Optional;

public interface FlashCardRepo {

    List<FlashCardData> findDataByDeckId(DeckId deckId);

    Optional<FlashCardReview> findReviewInfoByCard(FlashCardReview.FlashCardId id);

    List<FlashCardReview> findFlashCardsByDeckAndUser(DeckId deckId, String userId);


    void saveFlashCardState(FlashCardReview review);

    void createFlashCard(FlashCardReview flashCardReview);

    Optional<FlashCardReview> getCardAgainstContentAndUser(FlashCardReview.ContentId contentId,
                                                           ContentRefType contentType,
                                                           UserId userId);
}
