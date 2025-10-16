package com.myriadcode.languagelearner.flashcards.infrastructure.jpa.dao;

import com.myriadcode.languagelearner.flashcards.domain.models.DeckId;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards.domain.repos.FlashCardRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommonRepoImpl implements FlashCardRepo {

    // deckId → list of card data
    private final Map<String, List<FlashCardData>> deckData = new ConcurrentHashMap<>();

    // cardId → review state
    private final Map<String, FlashCardReview> reviewStates = new ConcurrentHashMap<>();

    public CommonRepoImpl() {
        // --- sample data initialization ---
        List<FlashCardData> germanDeck = List.of(
                new FlashCardData(new FlashCardData.FlashCardId("1"), "der Hund", "the dog", false),
                new FlashCardData(new FlashCardData.FlashCardId("2"), "die Katze", "the cat", false),
                new FlashCardData(new FlashCardData.FlashCardId("3"), "das Haus", "the house", false),
                new FlashCardData(new FlashCardData.FlashCardId("4"), "der Baum", "the tree", false)
        );
        deckData.put("german-basic", germanDeck);

        // no review states yet — all new cards are due immediately
    }
    @Override
    public List<FlashCardData> findDataByDeckId(DeckId deckId) {
        return deckData.getOrDefault(deckId.id(), List.of());
    }

    @Override
    public Optional<FlashCardReview> findReviewInfoByCard(FlashCardData.FlashCardId id) {
        return Optional.ofNullable(reviewStates.get(id.id()));
    }

    @Override
    public void saveFlashCardState(FlashCardReview review) {
        reviewStates.put(review.id().id(), review);

    }
}
