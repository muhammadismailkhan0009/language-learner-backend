package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FlashCardAlgorithmService {

    // Helper method to filter cards that are due for study
    private static List<FlashCardReview> filterDueCards(List<FlashCardReview> cards, Instant now) {
        return cards.stream()
                .filter(reviewData -> {
                    var due = reviewData.cardReviewData().due();
                    // Include cards that are overdue or due now
                    return due != null && due.isBefore(now);
                })
                .toList();
    }

    // this code gives us the next card to study based on the due date
    public static Optional<FlashCardReview> getNextCardToStudy(List<FlashCardReview> cards) {
        Instant now = Instant.now();
        var dueCards = filterDueCards(cards, now);

        return dueCards.stream()
                .min(Comparator
                        // Prioritize overdue cards first
                        .comparing((FlashCardReview r) -> r.cardReviewData().due().isBefore(now) ? 0 : 1)
                        // Then pick the one with the earliest due date
                        .thenComparing(r -> r.cardReviewData().due()));
    }

    public static Optional<FlashCardReview> getNextCardForRevision(
            List<FlashCardReview> cards,
            RevisionSession session
    ) {
        var result = getCardsForRevision(cards, session, 1);
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(result.get(0));
    }

    public static List<FlashCardReview> getCardsForRevision(
            List<FlashCardReview> cards,
            RevisionSession session,
            int count
    ) {
        if (cards.isEmpty()) return List.of();

        Instant now = Instant.now();

        // 1️⃣ Eligible cards (unchanged)
        var eligible = cards.stream()
                .filter(r -> {
                    var c = r.cardReviewData();
                    return c.state() != com.myriadcode.fsrs.api.enums.State.NEW
                            && c.due() != null
                            && c.due().isAfter(now);
                })
                .toList();

        if (eligible.isEmpty()) return List.of();

        // 2️⃣ Build buckets (unchanged)
        var weak = new ArrayList<FlashCardReview>();
        var medium = new ArrayList<FlashCardReview>();
        var strong = new ArrayList<FlashCardReview>();

        for (var r : eligible) {
            var c = r.cardReviewData();
            if (c.lapses() >= 2 || c.stability() <= 3) {
                weak.add(r);
            } else if (c.stability() <= 6) {
                medium.add(r);
            } else {
                strong.add(r);
            }
        }

        // 3️⃣ Get ordered buckets (NEW)
        var orderedBuckets = pickBucketsInOrder(weak, medium, strong);

        // 4️⃣ Try buckets sequentially (KEY FIX)
        for (var bucket : orderedBuckets) {
            if (bucket.isEmpty()) continue;

            var candidates = bucket.stream()
                    .filter(r -> !r.id().id().equals(session.lastShown()))
                    .filter(r -> session.shownTimes(r.id().id()) <= 2)
                    .toList();

            if (!candidates.isEmpty()) {
                return pickRandom(candidates, count);
            }
        }

        // 5️⃣ Final fallback: ignore session guards, but keep eligibility
        var fallback = eligible.stream().toList();
        if (fallback.isEmpty()) return List.of();

        return pickRandom(fallback, count);
    }

    private static List<FlashCardReview> pickRandom(
            List<FlashCardReview> source,
            int count
    ) {
        int actualCount = Math.min(count, source.size());
        if (actualCount <= 0) return List.of();

        var shuffled = new ArrayList<>(source);
        java.util.Collections.shuffle(shuffled, ThreadLocalRandom.current());

        return shuffled.subList(0, actualCount);
    }

    private static List<List<FlashCardReview>> pickBucketsInOrder(
            List<FlashCardReview> weak,
            List<FlashCardReview> medium,
            List<FlashCardReview> strong
    ) {
        int r = ThreadLocalRandom.current().nextInt(100);

        List<List<FlashCardReview>> order = new ArrayList<>(3);

        if (r < 50) {
            order.add(weak);
            order.add(medium);
            order.add(strong);
        } else if (r < 80) {
            order.add(medium);
            order.add(weak);
            order.add(strong);
        } else {
            order.add(strong);
            order.add(medium);
            order.add(weak);
        }

        return order;
    }

    public static List<FlashCardReview> getRandomCards(
            List<FlashCardReview> cards,
            int count
    ) {
        if (cards.isEmpty()) return List.of();

        Instant now = Instant.now();

        // 1️⃣ Reuse existing due-card logic (unchanged)
        var dueCards = filterDueCards(cards, now);
        if (dueCards.isEmpty()) return List.of();

        // 2️⃣ Sort by how overdue the card is (most overdue first)
        var sortedByOverdue = dueCards.stream()
                .sorted(Comparator.comparing(
                        (FlashCardReview r) -> java.time.Duration.between(
                                r.cardReviewData().due(),
                                now
                        )
                ).reversed())
                .toList();

        // 3️⃣ Take a limited window to keep randomness
        int windowSize = Math.min(sortedByOverdue.size(), count * 2);
        var window = new ArrayList<>(sortedByOverdue.subList(0, windowSize));

        // 4️⃣ Shuffle inside the window
        java.util.Collections.shuffle(window, ThreadLocalRandom.current());

        // 5️⃣ Return up to `count` cards
        return window.subList(0, Math.min(count, window.size()));
    }


}
