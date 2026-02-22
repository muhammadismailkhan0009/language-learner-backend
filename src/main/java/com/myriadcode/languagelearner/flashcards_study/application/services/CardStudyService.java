package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.enums.DeckInfo;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.concurnas_like_library.Value;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.flashcards_study.domain.algorithm.FlashCardAlgorithmService;
import com.myriadcode.languagelearner.flashcards_study.domain.algorithm.RevisionSession;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.vocabulary.VocabularyFlashCardData;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.domain.views.FlashCardView;
import com.myriadcode.languagelearner.flashcards_study.domain.views.VocabularyFlashCardView;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardStudyService {

    private final FsrsEngine scheduler = FsrsEngine.createDefault();

    private final FlashCardRepo flashCardRepo;
    private final FetchLanguageContentApi fetchLanguageContentApi;
    private final FetchPrivateVocabularyApi fetchPrivateVocabularyApi;

    private final RevisionSession revisionSession = new  RevisionSession();

    /**
     * @deprecated Legacy mixed-concern API that multiplexes decks.
     * Prefer explicit per-domain methods/endpoints and remove this once clients migrate.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public Optional<FlashCardView> getNextCardToStudy(DeckInfo deckId, String userId) {

        if (shouldGenerateLanguageContent(deckId)) {
            Vals.runIo(() -> fetchLanguageContentApi.generateCardsForUser(new UserId(userId)));
        }

//        move the logic to repo like repo.fetchNextCardToStudy
        var cards = Vals.io(() -> flashCardRepo.findFlashCardsByDeckAndUser(new DeckId(deckId.getId()), userId));
        if (cards.value().isEmpty()) return Optional.empty();

        // pick first due or new card
        var nextOptional = cards.mapCpu(FlashCardAlgorithmService::getNextCardToStudy);

        if (nextOptional.value().isEmpty()) return Optional.empty(); // nothing due

        var next = nextOptional.value().get();
        return mapToFlashCardView(next, userId, false);
    }

    /**
     * @deprecated Legacy generic review API. Prefer explicit per-capability review methods
     * (e.g. reviewVocabularyStudiedCard) and remove/refactor this later.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
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


    /**
     * @deprecated Legacy mixed-concern API that multiplexes decks.
     * Prefer explicit per-domain methods/endpoints and remove this once clients migrate.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public Optional<FlashCardView> getNextCardForRevision(
            DeckInfo deckId,
            String userId,
            Integer count
    ) {
        var cards = getCardsForRevision(deckId, userId, count);
        if (cards.isEmpty()) return Optional.empty();
        return Optional.of(cards.getFirst());
    }

    /**
     * @deprecated Legacy mixed-concern API that multiplexes decks.
     * Prefer explicit per-domain methods/endpoints and remove this once clients migrate.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
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
            var cardView = mapToFlashCardView(next, userId, true);
            cardView.ifPresent(view -> contentFetches.add(Vals.cpu(() -> view)));
        }

        // Extract all results (this will wait for all parallel IO operations to complete)
        return contentFetches.stream()
                .map(Value::value)
                .toList();
    }

    /**
     * @deprecated Legacy mixed-concern API that multiplexes decks.
     * Prefer explicit per-domain methods/endpoints and remove this once clients migrate.
     */
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public List<FlashCardView> getNextCardsToStudy(DeckInfo deckId, String userId, int count) {

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
            var cardView = mapToFlashCardView(next, userId, false);
            cardView.ifPresent(view -> contentFetches.add(Vals.cpu(() -> view)));
        }

        // Extract all results (this will wait for all parallel IO operations to complete)
        return contentFetches.stream()
                .map(Value::value)
                .toList();
    }

    public List<VocabularyFlashCardView> getNextPrivateVocabularyCardsToStudy(String userId, int count) {
        var cards = flashCardRepo.findVocabularyFlashCardsByUser(userId);
        if (cards.isEmpty()) return List.of();

        var randomCards = FlashCardAlgorithmService.getRandomCards(cards, 3);
        if (randomCards.isEmpty()) return List.of();

        return randomCards.stream()
                .findAny()
                .map(review -> toVocabularyFlashCardData(review, fetchPrivateVocabularyApi.getVocabularyRecord(review.contentId().id(), userId)))
                .map(data -> toVocabularyFlashCardView(data, false))
                .stream()
                .toList();
    }

    public List<VocabularyFlashCardView> getPrivateVocabularyCardsForRevision(String userId, int count) {
        var cards = flashCardRepo.findVocabularyFlashCardsByUser(userId);
        if (cards.isEmpty()) return List.of();

        var revisionCards = FlashCardAlgorithmService.getCardsForRevision(cards, revisionSession, count);
        if (revisionCards.isEmpty()) return List.of();

        for (var card : revisionCards) {
            revisionSession.markShown(card.id().id());
        }

        return revisionCards.stream()
                .map(review -> toVocabularyFlashCardData(review, fetchPrivateVocabularyApi.getVocabularyRecord(review.contentId().id(), userId)))
                .map(data -> toVocabularyFlashCardView(data, true))
                .toList();
    }

    public Optional<VocabularyFlashCardView> getNextPrivateVocabularyCardForRevision(String userId, int count) {
        var cards = getPrivateVocabularyCardsForRevision(userId, count);
        if (cards.isEmpty()) return Optional.empty();
        return Optional.of(cards.getFirst());
    }

    public void reviewVocabularyStudiedCard(String cardId, Rating rating) {
        var reviewData = flashCardRepo.findVocabularyReviewInfoByCard(
                new FlashCardReview.FlashCardId(cardId)
        );
        if (reviewData.isEmpty()) throw new RuntimeException("No vocabulary card found");

        Card updated = scheduler.reSchedule(reviewData.get().cardReviewData(), rating, Instant.now());

        var updatedReview = new FlashCardReview(
                reviewData.get().id(),
                reviewData.get().userId(),
                reviewData.get().contentId(),
                ContentRefType.VOCABULARY,
                updated,
                reviewData.get().isReversed()
        );

        flashCardRepo.saveVocabularyFlashCardState(updatedReview);
    }

    private Optional<FlashCardView> mapToFlashCardView(FlashCardReview next, String userId, boolean isRevision) {
        if (next.contentType().equals(ContentRefType.CHUNK)) {
            var data = Vals.io(() -> fetchLanguageContentApi.getChunkRecord(next.contentId().id()));
            return Optional.of(new FlashCardView(
                    next.id().id(),
                    new FlashCardView.Front(data.value().original()),
                    new FlashCardView.Back(data.value().translation()),
                    data.value().note(),
                    true,
                    isRevision
            ));
        }

        if (next.contentType().equals(ContentRefType.SENTENCE)) {
            var data = Vals.io(() -> fetchLanguageContentApi.getSentenceRecord(next.contentId().id()));
            return Optional.of(new FlashCardView(
                    next.id().id(),
                    new FlashCardView.Front(next.isReversed() ? data.value().translation() : data.value().original()),
                    new FlashCardView.Back(next.isReversed() ? data.value().original() : data.value().translation()),
                    null,
                    next.isReversed(),
                    isRevision
            ));
        }

        if (next.contentType().equals(ContentRefType.VOCABULARY)) {
            var data = fetchPrivateVocabularyApi.getVocabularyRecord(next.contentId().id(), userId);
            var vocabularyData = toVocabularyFlashCardData(next, data);
            return Optional.of(toLegacyVocabularyFlashCardView(vocabularyData, isRevision));
        }

        return Optional.empty();
    }

    private FlashCardView toLegacyVocabularyFlashCardView(VocabularyFlashCardData data,
                                                          boolean isRevision) {
        var examples = data.sentences()
                .stream()
                .map(VocabularyFlashCardData.SentenceData::sentence)
                .filter(sentence -> sentence != null && !sentence.isBlank())
                .map(sentenceText -> "- " + sentenceText)
                .collect(Collectors.joining("\n"));
        var backText = examples.isBlank()
                ? data.backWordOrChunk()
                : data.backWordOrChunk() + "\n\nBeispiele:\n" + examples;
        return new FlashCardView(
                data.id().id(),
                new FlashCardView.Front(data.frontWordOrChunk()),
                new FlashCardView.Back(backText),
                null,
                data.isReversed(),
                isRevision
        );
    }

    private VocabularyFlashCardData toVocabularyFlashCardData(FlashCardReview review,
                                                              PrivateVocabularyRecord data) {
        var frontWord = review.isReversed() ? data.translation() : data.surface();
        var backWord = review.isReversed() ? data.surface() : data.translation();
        var sentences = data.exampleSentences().stream()
                .map(sentence -> new VocabularyFlashCardData.SentenceData(
                        sentence.id(),
                        sentence.sentence(),
                        sentence.translation()
                ))
                .toList();
        return new VocabularyFlashCardData(
                review.id(),
                frontWord,
                backWord,
                sentences,
                review.isReversed()
        );
    }

    private VocabularyFlashCardView toVocabularyFlashCardView(VocabularyFlashCardData data,
                                                              boolean isRevision) {
        var sentences = data.sentences().stream()
                .map(sentence -> new VocabularyFlashCardView.Sentence(
                        sentence.id(),
                        sentence.sentence(),
                        sentence.translation()
                ))
                .toList();
        return new VocabularyFlashCardView(
                data.id().id(),
                new VocabularyFlashCardView.Front(data.frontWordOrChunk()),
                new VocabularyFlashCardView.Back(data.backWordOrChunk(), sentences),
                data.isReversed(),
                isRevision
        );
    }

    private boolean shouldGenerateLanguageContent(DeckInfo deckId) {
        return DeckInfo.SENTENCES.equals(deckId) || DeckInfo.SENTENCES_REVISION.equals(deckId);
    }
}
