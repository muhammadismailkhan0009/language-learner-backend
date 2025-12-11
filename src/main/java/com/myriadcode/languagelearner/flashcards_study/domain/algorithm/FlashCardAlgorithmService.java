package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FlashCardAlgorithmService {

    public static Optional<FlashCardReview> getNextCardToStudy(List<FlashCardReview> cards) {
        Instant now = Instant.now();
        Instant upperBound = now.plus(1, ChronoUnit.DAYS);

        return cards.stream()
                .filter(reviewData -> {
                    var due = reviewData.cardReviewData().due();
                    // Include cards that are overdue or due within next 2 days
                    return due != null && !due.isAfter(now);
                })
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
        Instant now = Instant.now();

        // 1️⃣ Eligible cards
        var eligible = cards.stream()
                .filter(r -> {
                    var c = r.cardReviewData();
                    return c.state() != com.myriadcode.fsrs.api.enums.State.NEW
                            && c.due() != null
                            && c.due().isAfter(now);
                })
                .toList();

        if (eligible.isEmpty()) return Optional.empty();

        // 2️⃣ Bucket cards
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

        // 3️⃣ Pick bucket (50/30/20)
        List<FlashCardReview> bucket =
                pickBucket(weak, medium, strong);

        // fallback if bucket empty
        if (bucket.isEmpty()) {
            bucket = eligible;
        }

        // 4️⃣ Apply soft repetition guards
        var candidates = bucket.stream()
                .filter(r -> !r.id().id().equals(session.lastShown()))
                .filter(r -> session.shownTimes(r.id().id()) <= 2)
                .toList();

        if (candidates.isEmpty()) {
            candidates = bucket; // fail-open
        }

        // 5️⃣ Random pick
        var picked = candidates.get(
                java.util.concurrent.ThreadLocalRandom
                        .current()
                        .nextInt(candidates.size())
        );

        return Optional.of(picked);
    }

    private static List<FlashCardReview> pickBucket(
            List<FlashCardReview> weak,
            List<FlashCardReview> medium,
            List<FlashCardReview> strong
    ) {
        int r = ThreadLocalRandom.current().nextInt(100);

        if (r < 50 && !weak.isEmpty()) return weak;
        if (r < 80 && !medium.isEmpty()) return medium;
        if (!strong.isEmpty()) return strong;

        // spillover
        if (!weak.isEmpty()) return weak;
        if (!medium.isEmpty()) return medium;
        return strong;
    }


}
