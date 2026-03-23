package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VocabularyClozeSelectionPolicy {

    public static final int MAX_WORDS = 50;
    public static final int FORCED_NEW_CARD_COUNT = (int) (MAX_WORDS * 0.20);

    public List<VocabularyClozeCandidate> selectCandidates(String userId,
                                                           List<VocabularyClozeCandidate> candidates,
                                                           Instant rotationHour) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        var ranked = candidates.stream()
                .sorted(fsrsPriorityComparator(rotationHour))
                .toList();

        var count = Math.min(MAX_WORDS, ranked.size());
        return takeWithNewCap(ranked, count);
    }

    private Comparator<VocabularyClozeCandidate> fsrsPriorityComparator(Instant now) {
        return Comparator
                .comparing((VocabularyClozeCandidate candidate) -> dueBucket(candidate, now))
                .thenComparing(candidate -> overdueDurationOrZero(candidate, now), Comparator.reverseOrder())
                .thenComparing(VocabularyClozeCandidate::retrievability)
                .thenComparing(candidate -> timeUntilDueOrMax(candidate, now))
                .thenComparing(this::lastReviewOrEpoch)
                .thenComparing(VocabularyClozeCandidate::lapses, Comparator.reverseOrder())
                .thenComparingInt(candidate -> statePriority(candidate.state()));
    }

    private List<VocabularyClozeCandidate> takeWithNewCap(List<VocabularyClozeCandidate> candidates, int count) {
        if (count <= 0 || candidates.isEmpty()) {
            return List.of();
        }

        var selected = new ArrayList<VocabularyClozeCandidate>(count);
        int availableNewCards = (int) candidates.stream().filter(candidate -> candidate.state() == State.NEW).count();
        int forcedNewTarget = Math.min(Math.min(FORCED_NEW_CARD_COUNT, availableNewCards), count);
        int selectedNewCards = 0;

        // Prefer generation for already-learned states first.
        for (var candidate : candidates) {
            if (selected.size() >= count) {
                break;
            }
            if (candidate.state() == State.NEW) {
                continue;
            }
            selected.add(candidate);
        }

        // Add NEW cards up to forced target when slots are still available.
        for (var candidate : candidates) {
            if (selected.size() >= count || selectedNewCards >= forcedNewTarget) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            if (candidate.state() != State.NEW) {
                continue;
            }
            selected.add(candidate);
            selectedNewCards++;
        }

        // If full already, replace lowest-priority non-NEW entries to satisfy forced NEW target.
        if (selectedNewCards < forcedNewTarget && selected.size() >= count) {
            for (var candidate : candidates) {
                if (selectedNewCards >= forcedNewTarget) {
                    break;
                }
                if (candidate.state() != State.NEW || selected.contains(candidate)) {
                    continue;
                }
                int replacementIndex = lastNonNewIndex(selected);
                if (replacementIndex < 0) {
                    break;
                }
                selected.set(replacementIndex, candidate);
                selectedNewCards++;
            }
        }

        // Fill remaining slots with best-ranked remaining cards.
        for (var candidate : candidates) {
            if (selected.size() >= count) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            selected.add(candidate);
        }

        return selected;
    }

    private int lastNonNewIndex(List<VocabularyClozeCandidate> selected) {
        for (int i = selected.size() - 1; i >= 0; i--) {
            if (selected.get(i).state() != State.NEW) {
                return i;
            }
        }
        return -1;
    }

    private int dueBucket(VocabularyClozeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return 2;
        }
        return candidate.due().isAfter(now) ? 1 : 0;
    }

    private java.time.Duration overdueDurationOrZero(VocabularyClozeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return java.time.Duration.ZERO;
        }
        return candidate.due().isAfter(now)
                ? java.time.Duration.ZERO
                : java.time.Duration.between(candidate.due(), now);
    }

    private java.time.Duration timeUntilDueOrMax(VocabularyClozeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return java.time.Duration.ofDays(36500);
        }
        return java.time.Duration.between(now, candidate.due()).abs();
    }

    private Instant lastReviewOrEpoch(VocabularyClozeCandidate candidate) {
        return candidate.lastReview() == null ? Instant.EPOCH : candidate.lastReview();
    }

    private int statePriority(State state) {
        if (state == null) {
            return 4;
        }
        return switch (state) {
            case RE_LEARNING -> 0;
            case LEARNING -> 1;
            case REVIEW -> 2;
            case NEW -> 3;
        };
    }
}
