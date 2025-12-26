package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.concurnas_like_library.Value;
import com.myriadcode.languagelearner.flashcards_study.domain.algorithm.FlashCardAlgorithmService;
import com.myriadcode.languagelearner.flashcards_study.domain.algorithm.RevisionSession;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardStudyService {

    private final FsrsEngine scheduler = FsrsEngine.createDefault();

    private final FlashCardRepo flashCardRepo;
    private final FetchLanguageContentApi fetchLanguageContentApi;

    private final RevisionSession revisionSession = new  RevisionSession();

    //    TODO: refactor it such as its tests can be written properly
    public Optional<FlashCardView> getNextCardToStudy(DeckInfo deckId, String userId) {

        Vals.runIo(() -> fetchLanguageContentApi.generateCardsForUser(new UserId(userId)));

//        move the logic to repo like repo.fetchNextCardToStudy
        var cards = Vals.io(() -> flashCardRepo.findFlashCardsByDeckAndUser(new DeckId(deckId.getId()), userId));
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
                    true,
                    false
            ));
        } else if (next.contentType().equals(ContentRefType.SENTENCE)) {
            var data = Vals.io(() -> fetchLanguageContentApi.getSentenceRecord(next.contentId().id()));
            return Optional.of(new FlashCardView(
                    next.id().id(),
                    new FlashCardView.Front(next.isReversed()?data.value().translation():data.value().original()),
                    new FlashCardView.Back(next.isReversed()?data.value().original():data.value().translation()),
                    null,
                    next.isReversed(),
                    false
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


    public Optional<FlashCardView> getNextCardForRevision(
            DeckInfo deckId,
            String userId,
            Integer count
    ) {
        var cards = getCardsForRevision(deckId, userId, count);
        if (cards.isEmpty()) return Optional.empty();
        return Optional.of(cards.get(ThreadLocalRandom.current().nextInt(0, cards.size() + 1)));
    }

    public List<FlashCardView> getCardsForRevision(
            DeckInfo deckId,
            String userId,
            int count
    ) {
        var cards = Vals.io(() ->
                flashCardRepo.findFlashCardsByDeckAndUser(
                        new DeckId(deckId.getId()), userId));

        if (cards.value().isEmpty()) return List.of();

        // Apply algorithm to get revision cards (CPU operation)
        var revisionCards = cards.mapCpu(cardList -> 
                FlashCardAlgorithmService.getCardsForRevision(cardList, revisionSession, count));
        
        if (revisionCards.value().isEmpty()) return List.of();
        System.out.println("cards for revision..............................");
        // Mark all cards as shown in the session
        for (var card : revisionCards.value()) {
            revisionSession.markShown(card.id().id());
        }

        // Create parallel IO operations for fetching content for each card
        // This allows all content fetches to run concurrently instead of sequentially
        var contentFetches = new ArrayList<Value<FlashCardView>>();
        
        for (var next : revisionCards.value()) {
            if (next.contentType().equals(ContentRefType.CHUNK)) {
                System.out.println("id::" + next.contentId().id());
                var data = Vals.io(() -> fetchLanguageContentApi.getChunkRecord(next.contentId().id()));
                var flashCardView = data.map(content -> new FlashCardView(
                        next.id().id(),
                        new FlashCardView.Front(content.original()),
                        new FlashCardView.Back(content.translation()),
                        content.note(),
                        true,
                        true
                ));
                contentFetches.add(flashCardView);
            } else if (next.contentType().equals(ContentRefType.SENTENCE)) {
                var data = Vals.io(() -> fetchLanguageContentApi.getSentenceRecord(next.contentId().id()));
                var flashCardView = data.map(content -> new FlashCardView(
                        next.id().id(),
                        new FlashCardView.Front(next.isReversed() ? content.translation() : content.original()),
                        new FlashCardView.Back(next.isReversed() ? content.original() : content.translation()),
                        null,
                        next.isReversed(),
                        true
                ));
                contentFetches.add(flashCardView);
            }
        }

        // Extract all results (this will wait for all parallel IO operations to complete)
        return contentFetches.stream()
                .map(Value::value)
                .toList();
    }

    public List<FlashCardView> getNextCardsToStudy(DeckInfo deckId, String userId, int count) {
        // Generate cards asynchronously (fire and forget, like getNextCardToStudy)
        Vals.runIo(() -> fetchLanguageContentApi.generateCardsForUser(new UserId(userId)));

        // Fetch cards from repository (IO operation)
        var cards = Vals.io(() -> flashCardRepo.findFlashCardsByDeckAndUser(new DeckId(deckId.getId()), userId));
        if (cards.value().isEmpty()) return List.of();

        // Apply algorithm to get random due cards (CPU operation)
        var randomCards = cards.mapCpu(cardList -> FlashCardAlgorithmService.getRandomCards(cardList, count));
        if (randomCards.value().isEmpty()) return List.of();

        // Create parallel IO operations for fetching content for each card
        // This allows all content fetches to run concurrently instead of sequentially
        var contentFetches = new ArrayList<Value<FlashCardView>>();
        
        for (var next : randomCards.value()) {
            if (next.contentType().equals(ContentRefType.CHUNK)) {
                System.out.println("id::" + next.contentId().id());
                var data = Vals.io(() -> fetchLanguageContentApi.getChunkRecord(next.contentId().id()));
                var flashCardView = data.map(content -> new FlashCardView(
                        next.id().id(),
                        new FlashCardView.Front(content.original()),
                        new FlashCardView.Back(content.translation()),
                        content.note(),
                        true,
                        false
                ));
                contentFetches.add(flashCardView);
            } else if (next.contentType().equals(ContentRefType.SENTENCE)) {
                var data = Vals.io(() -> fetchLanguageContentApi.getSentenceRecord(next.contentId().id()));
                var flashCardView = data.map(content -> new FlashCardView(
                        next.id().id(),
                        new FlashCardView.Front(next.isReversed() ? content.translation() : content.original()),
                        new FlashCardView.Back(next.isReversed() ? content.original() : content.translation()),
                        null,
                        next.isReversed(),
                        false
                ));
                contentFetches.add(flashCardView);
            }
        }

        // Extract all results (this will wait for all parallel IO operations to complete)
        return contentFetches.stream()
                .map(Value::value)
                .toList();
    }

}
