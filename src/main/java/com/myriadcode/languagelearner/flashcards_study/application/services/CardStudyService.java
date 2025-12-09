package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardStudyService {

    private final FsrsEngine scheduler = FsrsEngine.createDefault();

    private final FlashCardRepo flashCardRepo;

    private final FetchLanguageContentApi fetchLanguageContentApi;

    //    TODO: refactor it such as its tests can be written properly
    public Optional<FlashCardView> getNextCardToStudy(String deckId, String userId) {

        Vals.runIo(() -> fetchLanguageContentApi.generateCardsForUser(new UserId(userId)));

        System.out.println(userId);
//        move the logic to repo like repo.fetchNextCardToStudy
        var cards = Vals.io(() -> flashCardRepo.findFlashCardsByDeckAndUser(new DeckId(deckId), userId));
        if (cards.value().isEmpty()) return Optional.empty();

        // pick first due or new card
        var nextOptional = cards.mapCpu(FlashCardAlgorithmService::getNextCardToStudy);

        if (nextOptional.value().isEmpty()) return Optional.empty(); // nothing due

        var next = nextOptional.value().get();

//        check and create cards for new user(just an event to run n async mode)

        if (next.contentType().equals(ContentRefType.CHUNK)) {
            System.out.println("id::" + next.contentId().id());
            var data = Vals.io(() -> fetchLanguageContentApi.getChunkRecord(next.contentId().id()));
            return Optional.of(new FlashCardView(
                    next.id().id(),
                    new FlashCardView.Front(data.value().original()),
                    new FlashCardView.Back(data.value().translation()),
                    data.value().note(),
                    true
            ));
        } else if (next.contentType().equals(ContentRefType.SENTENCE)) {
            var data = Vals.io(() -> fetchLanguageContentApi.getSentenceRecord(next.contentId().id()));
            return Optional.of(new FlashCardView(
                    next.id().id(),
                    new FlashCardView.Front(next.isReversed()?data.value().translation():data.value().original()),
                    new FlashCardView.Back(next.isReversed()?data.value().original():data.value().translation()),
                    null,
                    next.isReversed()
            ));
        }


        return Optional.empty();
    }

    //    this is orchestrator, not business logic place. move logic to repo or some place else.
    public void reviewStudiedCard(String cardId, Rating rating) {
        var reviewData = flashCardRepo.findReviewInfoByCard(new FlashCardReview.FlashCardId(cardId));
        if (reviewData.isEmpty()) throw new RuntimeException("No card found");

        Card state = reviewData.get().cardReviewData();

        Card updated = scheduler.reSchedule(state, rating, Instant.now());

        var updatedReview = new FlashCardReview(
                new FlashCardReview.FlashCardId(cardId),
                reviewData.get().userId(),
                reviewData.get().contentId(),
                reviewData.get().contentType(),
                updated,
                reviewData.get().isReversed());

        flashCardRepo.saveFlashCardState(updatedReview);

    }
}
