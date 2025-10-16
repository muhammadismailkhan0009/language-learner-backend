package com.myriadcode.languagelearner.flashcards.application.services;

import com.myriadcode.languagelearner.flashcards.domain.models.DeckId;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards.domain.views.FlashCardView;
import io.github.openspacedrepetition.Card;
import io.github.openspacedrepetition.CardAndReviewLog;
import io.github.openspacedrepetition.Rating;
import io.github.openspacedrepetition.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Optional;

@Service
public class CardStudyService {

    private final Scheduler scheduler = Scheduler.builder().build();

    @Autowired
    private FlashCardRepo flashCardRepo;

    public Optional<FlashCardView> getNextCardToStudy(String deckId) {

        var cards = flashCardRepo.findDataByDeckId(new DeckId(deckId));
        if (cards.isEmpty()) return Optional.empty();

        Instant now = Instant.now();

        // pick first due or new card
//        FIXME: extract the db calls outside in batch, and use that batch inside.
        var next = cards.stream()
                .filter(c -> {
                    var reviewData = flashCardRepo.findReviewInfoByCard(c.id());
                    if (reviewData.isEmpty()) return true; // new card
                    var due = reviewData.get().cardReviewData().getDue();
                    return due == null || !due.isAfter(now.plus(7, ChronoUnit.DAYS));
                })
                .min(Comparator.comparing(c -> {
                    var reviewData = flashCardRepo.findReviewInfoByCard(c.id());

                    if (reviewData.isEmpty()) return Instant.EPOCH;
                    var cardState = reviewData.get().cardReviewData();
                    return cardState.getDue() == null ? Instant.EPOCH : cardState.getDue();
                }))
                .orElse(null);

        if (next == null) return Optional.empty(); // nothing due

        return Optional.of(new FlashCardView(
                next.id().id(),
                new FlashCardView.Front(next.frontText()),
                new FlashCardView.Back(next.backText()),
                next.isReversed(),
                null,
                null
        ));
    }

    public void reviewStudiedCard(String cardId, Rating rating) {
        var reviewData = flashCardRepo.findReviewInfoByCard(new FlashCardData.FlashCardId(cardId));
        Card state = reviewData.isPresent()
                ? reviewData.get().cardReviewData()
                : Card.builder().build(); // new cards are due immediately

        CardAndReviewLog result = scheduler.reviewCard(state, rating);
        Card updated = result.card();

        var updatedReview = new FlashCardReview(new FlashCardData.FlashCardId(cardId), updated);
        flashCardRepo.saveFlashCardState(updatedReview);

    }
}
