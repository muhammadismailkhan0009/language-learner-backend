package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FlashCardAlgorithmService {

    public static Optional<FlashCardReview> getNextCardToStudy(List<FlashCardReview> cards) {
        Instant now = Instant.now();
        Instant upperBound = now.plus(2, ChronoUnit.DAYS);

        return cards.stream()
                .filter(reviewData -> {
                    var due = reviewData.cardReviewData().due();
                    // Include cards that are overdue or due within next 2 days
                    return due != null && !due.isAfter(upperBound);
                })
                .min(Comparator
                        // Prioritize overdue cards first
                        .comparing((FlashCardReview r) -> r.cardReviewData().due().isBefore(now) ? 0 : 1)
                        // Then pick the one with the earliest due date
                        .thenComparing(r -> r.cardReviewData().due()));
    }
}
