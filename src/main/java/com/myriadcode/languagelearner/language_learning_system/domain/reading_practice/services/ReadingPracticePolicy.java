package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReadingPracticePolicy {

    public static final int MAX_WORDS = 20;
    public static final int MAX_NEW_CARDS = 2;
    public static final int REVIEW_COUNT = 8;
    public static final int RE_LEARNING_COUNT = 4;
    public static final int LEARNING_COUNT = 4;
    public static final int FLEX_COUNT = 2;
    public static final int MAX_VERY_WEAK_CARDS = 10;

    public List<ReadingPracticeCandidate> selectCandidates(String userId,
                                                           List<ReadingPracticeCandidate> candidates,
                                                           Instant rotationHour) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        var grouped = groupByState(candidates, rotationHour);
        var selected = new ArrayList<ReadingPracticeCandidate>(Math.min(MAX_WORDS, candidates.size()));

        addCandidates(selected, grouped.get(State.REVIEW), REVIEW_COUNT);
        addCandidates(selected, grouped.get(State.RE_LEARNING), RE_LEARNING_COUNT);
        addCandidates(selected, grouped.get(State.LEARNING), LEARNING_COUNT);
        addCandidates(selected, grouped.get(State.NEW), MAX_NEW_CARDS);

        var remainder = candidates.stream()
                .filter(candidate -> !selected.contains(candidate))
                .sorted(fsrsPriorityComparator(rotationHour))
                .toList();

        addCandidates(selected, remainder, FLEX_COUNT);
        addCandidates(selected, remainder, Math.min(MAX_WORDS, candidates.size()) - selected.size());

        return selected.subList(0, Math.min(selected.size(), Math.min(MAX_WORDS, candidates.size())));
    }

    private Map<State, List<ReadingPracticeCandidate>> groupByState(List<ReadingPracticeCandidate> candidates,
                                                                    Instant now) {
        var grouped = new EnumMap<State, List<ReadingPracticeCandidate>>(State.class);
        for (State state : State.values()) {
            grouped.put(state, new ArrayList<>());
        }
        for (var candidate : candidates) {
            grouped.computeIfAbsent(candidate.state(), ignored -> new ArrayList<>()).add(candidate);
        }
        for (var entry : grouped.entrySet()) {
            entry.getValue().sort(fsrsPriorityComparator(now));
        }
        return grouped;
    }

    private Comparator<ReadingPracticeCandidate> fsrsPriorityComparator(Instant now) {
        return Comparator
                .comparing((ReadingPracticeCandidate candidate) -> dueBucket(candidate, now))
                .thenComparing(candidate -> overdueDurationOrZero(candidate, now), Comparator.reverseOrder())
                .thenComparing(candidate -> timeUntilDueOrMax(candidate, now))
                .thenComparing(ReadingPracticeCandidate::stability)
                .thenComparing(this::lastReviewOrEpoch)
                .thenComparing(ReadingPracticeCandidate::lapses, Comparator.reverseOrder())
                .thenComparing(ReadingPracticeCandidate::difficulty, Comparator.reverseOrder())
                .thenComparingInt(candidate -> statePriority(candidate.state()))
                .thenComparing(ReadingPracticeCandidate::vocabularyCreatedAt)
                .thenComparing(ReadingPracticeCandidate::flashCardId);
    }

    private void addCandidates(List<ReadingPracticeCandidate> selected,
                               List<ReadingPracticeCandidate> candidates,
                               int targetCount) {
        if (targetCount <= 0 || candidates == null || candidates.isEmpty()) {
            return;
        }

        for (var candidate : candidates) {
            if (selected.size() >= MAX_WORDS) {
                break;
            }
            if (targetCount <= 0) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            if (candidate.state() == State.NEW && selectedNewCount(selected) >= MAX_NEW_CARDS) {
                continue;
            }
            if (isVeryWeak(candidate) && selectedVeryWeakCount(selected) >= MAX_VERY_WEAK_CARDS) {
                continue;
            }
            selected.add(candidate);
            targetCount--;
        }
    }

    private int selectedNewCount(List<ReadingPracticeCandidate> selected) {
        return (int) selected.stream().filter(candidate -> candidate.state() == State.NEW).count();
    }

    private int selectedVeryWeakCount(List<ReadingPracticeCandidate> selected) {
        return (int) selected.stream().filter(this::isVeryWeak).count();
    }

    private boolean isVeryWeak(ReadingPracticeCandidate candidate) {
        return candidate.lapses() >= 2 || candidate.stability() <= 2.5;
    }

    private int dueBucket(ReadingPracticeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return 2;
        }
        return candidate.due().isAfter(now) ? 1 : 0;
    }

    private java.time.Duration overdueDurationOrZero(ReadingPracticeCandidate candidate, Instant now) {
        if (candidate.due() == null || candidate.due().isAfter(now)) {
            return java.time.Duration.ZERO;
        }
        return java.time.Duration.between(candidate.due(), now);
    }

    private java.time.Duration timeUntilDueOrMax(ReadingPracticeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return java.time.Duration.ofDays(36500);
        }
        return java.time.Duration.between(now, candidate.due()).abs();
    }

    private Instant lastReviewOrEpoch(ReadingPracticeCandidate candidate) {
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
