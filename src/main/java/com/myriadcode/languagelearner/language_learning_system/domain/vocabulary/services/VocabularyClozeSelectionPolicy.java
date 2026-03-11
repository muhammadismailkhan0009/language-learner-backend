package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VocabularyClozeSelectionPolicy {

    public static final int MAX_WORDS = 20;
    public static final int MAX_NEW_CARDS = 3;

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
        var windowSize = Math.min(ranked.size(), Math.max(count * 3, count + 4));
        var window = ranked.subList(0, windowSize);

        return takeWithNewCap(window, count);
    }

    private Comparator<VocabularyClozeCandidate> fsrsPriorityComparator(Instant now) {
        return Comparator
                .comparing((VocabularyClozeCandidate candidate) -> dueBucket(candidate, now))
                .thenComparing(candidate -> overdueDurationOrZero(candidate, now), Comparator.reverseOrder())
                .thenComparing(candidate -> timeUntilDueOrMax(candidate, now))
                .thenComparing(VocabularyClozeCandidate::stability)
                .thenComparing(this::lastReviewOrEpoch)
                .thenComparing(VocabularyClozeCandidate::lapses, Comparator.reverseOrder())
                .thenComparing(VocabularyClozeCandidate::difficulty, Comparator.reverseOrder())
                .thenComparingInt(candidate -> statePriority(candidate.state()))
                .thenComparing(VocabularyClozeCandidate::vocabularyCreatedAt)
                .thenComparing(VocabularyClozeCandidate::flashcardId);
    }

    private List<VocabularyClozeCandidate> takeWithNewCap(List<VocabularyClozeCandidate> candidates, int count) {
        if (count <= 0 || candidates.isEmpty()) {
            return List.of();
        }

        var maxNewCards = Math.min(MAX_NEW_CARDS, count);
        var selected = new ArrayList<VocabularyClozeCandidate>(count);
        var deferredNewCards = candidates.stream()
                .filter(candidate -> candidate.state() == State.NEW)
                .collect(Collectors.toCollection(ArrayList::new));

        for (var candidate : candidates) {
            if (selected.size() >= count) {
                break;
            }
            if (candidate.state() == State.NEW) {
                continue;
            }
            selected.add(candidate);
        }

        var selectedNewCards = (int) selected.stream().filter(candidate -> candidate.state() == State.NEW).count();
        for (var candidate : deferredNewCards) {
            if (selected.size() >= count || selectedNewCards >= maxNewCards) {
                break;
            }
            selected.add(candidate);
            selectedNewCards++;
        }

        if (selected.size() >= count) {
            return selected;
        }

        selectedNewCards = (int) selected.stream().filter(candidate -> candidate.state() == State.NEW).count();
        for (var candidate : candidates) {
            if (selected.size() >= count) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            if (candidate.state() == State.NEW && selectedNewCards >= maxNewCards) {
                continue;
            }
            selected.add(candidate);
            if (candidate.state() == State.NEW) {
                selectedNewCards++;
            }
        }

        return selected;
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
