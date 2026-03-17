package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class VocabularyListingArranger {

    private static final double WEAK_RETRIEVABILITY_THRESHOLD = 0.90;
    private static final double FRAGILE_REVIEW_RETRIEVABILITY_THRESHOLD = 0.92;

    private VocabularyListingArranger() {
    }

    static List<Vocabulary> arrange(List<Vocabulary> vocabularies,
                                    List<VocabularyFlashcardReviewRecord> flashcardStats,
                                    Instant referenceTime) {
        if (vocabularies == null || vocabularies.isEmpty()) {
            return List.of();
        }

        var statsByVocabularyId = flashcardStats == null
                ? Map.<String, List<VocabularyFlashcardReviewRecord>>of()
                : flashcardStats.stream()
                .filter(stat -> stat.vocabularyId() != null)
                .collect(Collectors.groupingBy(VocabularyFlashcardReviewRecord::vocabularyId));

        return vocabularies.stream()
                .map(vocabulary -> new RankedVocabulary(vocabulary, pickRepresentative(
                        statsByVocabularyId.get(vocabulary.id().id()),
                        referenceTime
                )))
                .sorted(priorityComparator(referenceTime))
                .map(RankedVocabulary::vocabulary)
                .toList();
    }

    private static VocabularyFlashcardReviewRecord pickRepresentative(
            List<VocabularyFlashcardReviewRecord> stats,
            Instant referenceTime
    ) {
        if (stats == null || stats.isEmpty()) {
            return null;
        }
        var preferredStats = stats.stream().anyMatch(VocabularyFlashcardReviewRecord::isReversed)
                ? stats.stream().filter(VocabularyFlashcardReviewRecord::isReversed).toList()
                : stats;
        return preferredStats.stream()
                .sorted(statComparator(referenceTime))
                .findFirst()
                .orElse(null);
    }

    private static Comparator<RankedVocabulary> priorityComparator(Instant referenceTime) {
        return Comparator
                .comparing((RankedVocabulary ranked) -> learningStatePriority(ranked.representative(), referenceTime))
                .thenComparing(ranked -> weaknessScore(ranked.representative(), referenceTime), Comparator.reverseOrder())
                .thenComparing(ranked -> overdueDurationOrZero(ranked.representative(), referenceTime), Comparator.reverseOrder())
                .thenComparing(ranked -> timeUntilDueOrMax(ranked.representative(), referenceTime))
                .thenComparing(ranked -> retrievabilityOrMax(ranked.representative()))
                .thenComparing(ranked -> lastReviewOrEpoch(ranked.representative()))
                .thenComparing(ranked -> lapsesOrZero(ranked.representative()), Comparator.reverseOrder())
                .thenComparing(ranked -> statePriority(ranked.representative(), referenceTime))
                .thenComparing(ranked -> createdAtOrEpoch(ranked.vocabulary()), Comparator.reverseOrder())
                .thenComparing(ranked -> ranked.vocabulary().id().id());
    }

    private static Comparator<VocabularyFlashcardReviewRecord> statComparator(Instant referenceTime) {
        return Comparator
                .comparing((VocabularyFlashcardReviewRecord stat) -> learningStatePriority(stat, referenceTime))
                .thenComparing(stat -> weaknessScore(stat, referenceTime), Comparator.reverseOrder())
                .thenComparing(stat -> overdueDurationOrZero(stat, referenceTime), Comparator.reverseOrder())
                .thenComparing(stat -> timeUntilDueOrMax(stat, referenceTime))
                .thenComparing(VocabularyListingArranger::retrievabilityOrMax)
                .thenComparing(VocabularyListingArranger::lastReviewOrEpoch)
                .thenComparing(VocabularyListingArranger::lapsesOrZero, Comparator.reverseOrder())
                .thenComparing(stat -> statePriority(stat, referenceTime))
                .thenComparing(VocabularyFlashcardReviewRecord::flashcardId);
    }

    private static int learningStatePriority(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (isWeak(stat, referenceTime)) {
            return 0;
        }
        if (isLearning(stat)) {
            return 1;
        }
        return 2;
    }

    private static boolean isWeak(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (stat == null) {
            return false;
        }
        return isDue(stat, referenceTime)
                || stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.RE_LEARNING
                || stat.lapses() > 0
                || isFragileReview(stat)
                || hasWeakRetrievability(stat);
    }

    private static boolean isLearning(VocabularyFlashcardReviewRecord stat) {
        if (stat == null) {
            return true;
        }
        if (isFragileReview(stat)) {
            return false;
        }
        return stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.LEARNING
                || stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.NEW;
    }

    private static boolean isFragileReview(VocabularyFlashcardReviewRecord stat) {
        return stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.REVIEW
                && hasKnownRetrievability(stat)
                && stat.retrievability() <= FRAGILE_REVIEW_RETRIEVABILITY_THRESHOLD;
    }

    private static double weaknessScore(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (stat == null) {
            return 0.0;
        }
        var score = 0.0;
        if (isDue(stat, referenceTime)) {
            score += 100.0;
        }
        if (stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.RE_LEARNING) {
            score += 40.0;
        }
        if (stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.LEARNING) {
            score += 20.0;
        }
        score += stat.lapses() * 10.0;
        if (hasKnownRetrievability(stat)) {
            score += (1.0 - stat.retrievability()) * 50.0;
        }
        return score;
    }

    private static boolean isDue(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        return stat != null && stat.due() != null && !stat.due().isAfter(referenceTime);
    }

    private static java.time.Duration overdueDurationOrZero(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (!isDue(stat, referenceTime)) {
            return java.time.Duration.ZERO;
        }
        return java.time.Duration.between(stat.due(), referenceTime);
    }

    private static java.time.Duration timeUntilDueOrMax(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (stat == null || stat.due() == null) {
            return java.time.Duration.ofDays(36500);
        }
        return java.time.Duration.between(referenceTime, stat.due()).abs();
    }

    private static double retrievabilityOrMax(VocabularyFlashcardReviewRecord stat) {
        if (!hasKnownRetrievability(stat)) {
            return Double.MAX_VALUE;
        }
        return stat.retrievability();
    }

    private static Instant lastReviewOrEpoch(VocabularyFlashcardReviewRecord stat) {
        return stat == null || stat.lastReview() == null ? Instant.EPOCH : stat.lastReview();
    }

    private static int lapsesOrZero(VocabularyFlashcardReviewRecord stat) {
        return stat == null ? 0 : stat.lapses();
    }

    private static boolean hasWeakRetrievability(VocabularyFlashcardReviewRecord stat) {
        return hasKnownRetrievability(stat) && stat.retrievability() <= WEAK_RETRIEVABILITY_THRESHOLD;
    }

    private static boolean hasKnownRetrievability(VocabularyFlashcardReviewRecord stat) {
        return stat != null && !Double.isNaN(stat.retrievability());
    }

    private static int statePriority(VocabularyFlashcardReviewRecord stat, Instant referenceTime) {
        if (stat == null || stat.fsrsState() == null) {
            return 4;
        }
        if (isWeak(stat, referenceTime)) {
            return switch (stat.fsrsState()) {
                case RE_LEARNING -> 0;
                case REVIEW -> 1;
                case LEARNING -> 2;
                case NEW -> 3;
            };
        }
        return switch (stat.fsrsState()) {
            case RE_LEARNING -> 0;
            case LEARNING -> 1;
            case REVIEW -> 2;
            case NEW -> 3;
        };
    }

    private static Instant createdAtOrEpoch(Vocabulary vocabulary) {
        return vocabulary.createdAt() == null ? Instant.EPOCH : vocabulary.createdAt();
    }

    private record RankedVocabulary(
            Vocabulary vocabulary,
            VocabularyFlashcardReviewRecord representative
    ) {
    }
}
