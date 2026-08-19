package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WritingPracticePolicy {

    private static final double FRAGILE_RETRIEVABILITY_THRESHOLD = 0.50;
    public static final int MAX_WORDS = 50;
    public static final double REVIEW_RATIO = 0.65;
    public static final double LEARNING_RATIO = 0.20;
    public static final double RE_LEARNING_RATIO = 0.15;
    public static final int MAX_FRAGILE_CARDS = 2;
    private static final List<State> STATE_ORDER = List.of(State.REVIEW, State.LEARNING, State.RE_LEARNING);

    public List<WritingPracticeCandidate> selectCandidates(List<WritingPracticeCandidate> candidates,
                                                           Instant now,
                                                           Map<String, Integer> recentUsageCounts) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        var eligible = candidates.stream()
                .filter(java.util.Objects::nonNull)
                .filter(candidate -> candidate.state() != null && candidate.state() != State.NEW)
                .toList();
        if (eligible.isEmpty()) {
            return List.of();
        }
        var safeNow = now == null ? Instant.EPOCH : now;
        var usageCounts = recentUsageCounts == null ? Map.<String, Integer>of() : recentUsageCounts;
        int capacity = Math.min(MAX_WORDS, eligible.size());
        var comparator = revisionComparator(safeNow, usageCounts);
        var grouped = groupAndSort(eligible, comparator);
        var selected = new ArrayList<WritingPracticeCandidate>(capacity);

        representStates(grouped, selected, capacity);
        var targets = calculateRatioTargets(capacity);
        for (var state : STATE_ORDER) {
            int remainingTarget = targets.getOrDefault(state, 0) - countState(selected, state);
            addCandidates(selected, grouped.get(state), remainingTarget, capacity);
        }

        var remaining = eligible.stream()
                .filter(candidate -> !selected.contains(candidate))
                .sorted(comparator)
                .toList();
        addCandidates(selected, remaining, capacity - selected.size(), capacity);
        return List.copyOf(selected);
    }

    private Map<State, List<WritingPracticeCandidate>> groupAndSort(
            List<WritingPracticeCandidate> candidates,
            Comparator<WritingPracticeCandidate> comparator
    ) {
        var grouped = new EnumMap<State, List<WritingPracticeCandidate>>(State.class);
        for (var state : STATE_ORDER) {
            grouped.put(state, new ArrayList<>());
        }
        candidates.forEach(candidate -> grouped.get(candidate.state()).add(candidate));
        grouped.values().forEach(bucket -> bucket.sort(comparator));
        return grouped;
    }

    private void representStates(Map<State, List<WritingPracticeCandidate>> grouped,
                                 List<WritingPracticeCandidate> selected,
                                 int capacity) {
        long nonEmptyStates = STATE_ORDER.stream().filter(state -> !grouped.get(state).isEmpty()).count();
        if (capacity < nonEmptyStates) {
            return;
        }
        for (var state : STATE_ORDER) {
            addCandidates(selected, grouped.get(state), 1, capacity);
        }
    }

    private void addCandidates(List<WritingPracticeCandidate> selected,
                               List<WritingPracticeCandidate> candidates,
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
            if (selected.contains(candidate) || exceedsFragileCap(selected, candidate)) {
                continue;
            }
            selected.add(candidate);
            added++;
        }
    }

    private boolean exceedsFragileCap(List<WritingPracticeCandidate> selected, WritingPracticeCandidate candidate) {
        return isFragile(candidate)
                && selected.stream().filter(this::isFragile).count() >= MAX_FRAGILE_CARDS;
    }

    private boolean isFragile(WritingPracticeCandidate candidate) {
        return !Double.isNaN(candidate.retrievability())
                && candidate.retrievability() <= FRAGILE_RETRIEVABILITY_THRESHOLD;
    }

    private Comparator<WritingPracticeCandidate> revisionComparator(
            Instant now,
            Map<String, Integer> usageCounts
    ) {
        return Comparator
                .comparingInt((WritingPracticeCandidate candidate) -> isOverdue(candidate, now) ? 0 : 1)
                .thenComparing(WritingPracticeCandidate::due, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingDouble(this::retrievabilityOrMax)
                .thenComparing(WritingPracticeCandidate::lapses, Comparator.reverseOrder())
                .thenComparing(WritingPracticeCandidate::lastReview, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparingInt(candidate -> usageCounts.getOrDefault(candidate.vocabularyId(), 0))
                .thenComparing(WritingPracticeCandidate::flashCardId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private boolean isOverdue(WritingPracticeCandidate candidate, Instant now) {
        return candidate.due() != null && !candidate.due().isAfter(now);
    }

    private double retrievabilityOrMax(WritingPracticeCandidate candidate) {
        return Double.isNaN(candidate.retrievability()) ? Double.POSITIVE_INFINITY : candidate.retrievability();
    }

    private int countState(List<WritingPracticeCandidate> selected, State state) {
        return (int) selected.stream().filter(candidate -> candidate.state() == state).count();
    }

    private Map<State, Integer> calculateRatioTargets(int count) {
        var ratios = new EnumMap<State, Double>(State.class);
        ratios.put(State.REVIEW, REVIEW_RATIO);
        ratios.put(State.LEARNING, LEARNING_RATIO);
        ratios.put(State.RE_LEARNING, RE_LEARNING_RATIO);
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
