package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class VocabularyListingArranger {

    private static final double WEAK_DIFFICULTY_THRESHOLD = 7.0;
    private static final double WEAK_STABILITY_THRESHOLD = 3.0;

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
        return stats.stream()
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
                .thenComparing(ranked -> stabilityOrMax(ranked.representative()))
                .thenComparing(ranked -> lastReviewOrEpoch(ranked.representative()))
                .thenComparing(ranked -> lapsesOrZero(ranked.representative()), Comparator.reverseOrder())
                .thenComparing(ranked -> difficultyOrZero(ranked.representative()), Comparator.reverseOrder())
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
                .thenComparing(VocabularyListingArranger::stabilityOrMax)
                .thenComparing(VocabularyListingArranger::lastReviewOrEpoch)
                .thenComparing(VocabularyListingArranger::lapsesOrZero, Comparator.reverseOrder())
                .thenComparing(VocabularyListingArranger::difficultyOrZero, Comparator.reverseOrder())
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
                || stat.difficulty() >= WEAK_DIFFICULTY_THRESHOLD
                || (stat.stability() > 0.0 && stat.stability() <= WEAK_STABILITY_THRESHOLD);
    }

    private static boolean isLearning(VocabularyFlashcardReviewRecord stat) {
        if (stat == null) {
            return true;
        }
        return stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.LEARNING
                || stat.fsrsState() == com.myriadcode.fsrs.api.enums.State.NEW;
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
        score += stat.difficulty();
        if (stat.stability() > 0.0) {
            score += Math.max(0.0, WEAK_STABILITY_THRESHOLD - stat.stability()) * 5.0;
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

    private static double stabilityOrMax(VocabularyFlashcardReviewRecord stat) {
        return stat == null ? Double.MAX_VALUE : stat.stability();
    }

    private static Instant lastReviewOrEpoch(VocabularyFlashcardReviewRecord stat) {
        return stat == null || stat.lastReview() == null ? Instant.EPOCH : stat.lastReview();
    }

    private static int lapsesOrZero(VocabularyFlashcardReviewRecord stat) {
        return stat == null ? 0 : stat.lapses();
    }

    private static double difficultyOrZero(VocabularyFlashcardReviewRecord stat) {
        return stat == null ? 0.0 : stat.difficulty();
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
