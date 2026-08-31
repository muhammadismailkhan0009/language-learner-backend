package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReadingPracticePolicy {

    private static final double VERY_WEAK_RETRIEVABILITY_THRESHOLD = 0.60;
    public static final int MAX_WORDS = 100;
    public static final double REVIEW_RATIO = 0.20;
    public static final double RE_LEARNING_RATIO = 0.35;
    public static final double LEARNING_RATIO = 0.30;
    public static final double NEW_RATIO = 0.15;
    public static final int MAX_VERY_WEAK_CARDS = 10;
    private static final List<State> STATE_ORDER = List.of(
            State.REVIEW, State.RE_LEARNING, State.LEARNING, State.NEW
    );

    public List<ReadingPracticeCandidate> selectCandidates(List<ReadingPracticeCandidate> candidates,
                                                           Instant now,
                                                           Map<String, Integer> recentUsageCounts) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        var safeNow = now == null ? Instant.EPOCH : now;
        var usageCounts = recentUsageCounts == null ? Map.<String, Integer>of() : recentUsageCounts;
        int capacity = Math.min(MAX_WORDS, candidates.size());
        var grouped = groupAndSort(candidates, safeNow, usageCounts);
        var selected = new ArrayList<ReadingPracticeCandidate>(capacity);

        representStates(grouped, selected, capacity);
        var targets = calculateRatioTargets(capacity);
        for (var state : STATE_ORDER) {
            int remainingTarget = targets.getOrDefault(state, 0) - countState(selected, state);
            addCandidates(selected, grouped.get(state), remainingTarget, capacity);
        }

        var remaining = candidates.stream()
                .filter(java.util.Objects::nonNull)
                .filter(candidate -> !selected.contains(candidate))
                .sorted(crossStateComparator(safeNow, usageCounts))
                .toList();
        addCandidates(selected, remaining, capacity - selected.size(), capacity);
        return List.copyOf(selected);
    }

    private Map<State, List<ReadingPracticeCandidate>> groupAndSort(
            List<ReadingPracticeCandidate> candidates,
            Instant now,
            Map<String, Integer> usageCounts
    ) {
        var grouped = new EnumMap<State, List<ReadingPracticeCandidate>>(State.class);
        for (var state : STATE_ORDER) {
            grouped.put(state, new ArrayList<>());
        }
        candidates.stream()
                .filter(java.util.Objects::nonNull)
                .filter(candidate -> candidate.state() != null)
                .forEach(candidate -> grouped.computeIfAbsent(candidate.state(), ignored -> new ArrayList<>()).add(candidate));
        grouped.forEach((state, bucket) -> bucket.sort(state == State.NEW
                ? newCardComparator(usageCounts)
                : revisionComparator(now, usageCounts)));
        return grouped;
    }

    private void representStates(Map<State, List<ReadingPracticeCandidate>> grouped,
                                 List<ReadingPracticeCandidate> selected,
                                 int capacity) {
        long nonEmptyStates = STATE_ORDER.stream().filter(state -> !grouped.get(state).isEmpty()).count();
        if (capacity < nonEmptyStates) {
            return;
        }
        for (var state : STATE_ORDER) {
            addCandidates(selected, grouped.get(state), 1, capacity);
        }
    }

    private void addCandidates(List<ReadingPracticeCandidate> selected,
                               List<ReadingPracticeCandidate> candidates,
                               int requested,
                               int capacity) {
        if (requested <= 0 || candidates == null || candidates.isEmpty()) {
            return;
        }
        int added = 0;
        for (var candidate : candidates) {
            if (selected.size() >= capacity || added >= requested) {
                return;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            selected.add(candidate);
            added++;
        }
    }

    @Deprecated(forRemoval = true)
    private boolean exceedsWeakCap(List<ReadingPracticeCandidate> selected, ReadingPracticeCandidate candidate) {
        return isVeryWeak(candidate)
                && selected.stream().filter(this::isVeryWeak).count() >= MAX_VERY_WEAK_CARDS;
    }

    private boolean isVeryWeak(ReadingPracticeCandidate candidate) {
        return candidate.lapses() >= 2
                || (!Double.isNaN(candidate.retrievability())
                && candidate.retrievability() <= VERY_WEAK_RETRIEVABILITY_THRESHOLD);
    }

    private Comparator<ReadingPracticeCandidate> crossStateComparator(
            Instant now,
            Map<String, Integer> usageCounts
    ) {
        return Comparator
                .comparingInt((ReadingPracticeCandidate candidate) -> candidate.state() == State.NEW ? 1 : 0)
                .thenComparing((left, right) -> {
                    if (left.state() == State.NEW && right.state() == State.NEW) {
                        return newCardComparator(usageCounts).compare(left, right);
                    }
                    return revisionComparator(now, usageCounts).compare(left, right);
                });
    }

    private Comparator<ReadingPracticeCandidate> revisionComparator(
            Instant now,
            Map<String, Integer> usageCounts
    ) {
        return Comparator
                .comparingInt((ReadingPracticeCandidate candidate) -> isOverdue(candidate, now) ? 0 : 1)
                .thenComparing(ReadingPracticeCandidate::due, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingDouble(this::retrievabilityOrMax)
                .thenComparing(ReadingPracticeCandidate::lapses, Comparator.reverseOrder())
                .thenComparing(ReadingPracticeCandidate::lastReview, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparingInt(candidate -> usageCounts.getOrDefault(candidate.vocabularyId(), 0))
                .thenComparing(ReadingPracticeCandidate::flashCardId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private Comparator<ReadingPracticeCandidate> newCardComparator(Map<String, Integer> usageCounts) {
        return Comparator
                .comparing(ReadingPracticeCandidate::vocabularyCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparingInt(candidate -> usageCounts.getOrDefault(candidate.vocabularyId(), 0))
                .thenComparing(ReadingPracticeCandidate::flashCardId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private boolean isOverdue(ReadingPracticeCandidate candidate, Instant now) {
        return candidate.due() != null && !candidate.due().isAfter(now);
    }

    private double retrievabilityOrMax(ReadingPracticeCandidate candidate) {
        return Double.isNaN(candidate.retrievability()) ? Double.POSITIVE_INFINITY : candidate.retrievability();
    }

    private int countState(List<ReadingPracticeCandidate> selected, State state) {
        return (int) selected.stream().filter(candidate -> candidate.state() == state).count();
    }

    private Map<State, Integer> calculateRatioTargets(int count) {
        var ratios = new EnumMap<State, Double>(State.class);
        ratios.put(State.REVIEW, REVIEW_RATIO);
        ratios.put(State.RE_LEARNING, RE_LEARNING_RATIO);
        ratios.put(State.LEARNING, LEARNING_RATIO);
        ratios.put(State.NEW, NEW_RATIO);
        var targets = new EnumMap<State, Integer>(State.class);
        int assigned = 0;
        for (var state : STATE_ORDER) {
            int target = (int) Math.floor(ratios.get(state) * count);
            targets.put(state, target);
            assigned += target;
        }
        for (int index = 0; assigned < count; index++, assigned++) {
            var state = STATE_ORDER.get(index % STATE_ORDER.size());
            targets.put(state, targets.get(state) + 1);
        }
        return targets;
    }
}
