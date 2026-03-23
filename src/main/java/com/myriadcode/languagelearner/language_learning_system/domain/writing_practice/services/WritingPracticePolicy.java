package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WritingPracticePolicy {

    private static final double FRAGILE_RETRIEVABILITY_THRESHOLD = 0.50;
    public static final int MAX_WORDS = 50;
    public static final double REVIEW_RATIO = 0.65;
    public static final double LEARNING_RATIO = 0.20;
    public static final double RE_LEARNING_RATIO = 0.15;
    public static final int MAX_FRAGILE_CARDS = 2;

    public List<WritingPracticeCandidate> selectCandidates(String userId,
                                                           List<WritingPracticeCandidate> candidates,
                                                           Instant rotationHour) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        var eligible = candidates.stream()
                .filter(candidate -> candidate.state() != State.NEW)
                .toList();
        if (eligible.isEmpty()) {
            return List.of();
        }
        var maxSelectable = Math.min(MAX_WORDS, eligible.size());

        var grouped = groupByState(eligible, rotationHour, userId);
        var selected = new ArrayList<WritingPracticeCandidate>(maxSelectable);
        var targets = calculateRatioTargets(maxSelectable);

        addCandidates(selected, grouped.get(State.REVIEW), targets.getOrDefault(State.REVIEW, 0));
        addCandidates(selected, grouped.get(State.LEARNING), targets.getOrDefault(State.LEARNING, 0));
        addCandidates(selected, grouped.get(State.RE_LEARNING), targets.getOrDefault(State.RE_LEARNING, 0));

        return selected.subList(0, Math.min(selected.size(), maxSelectable));
    }

    private Map<State, List<WritingPracticeCandidate>> groupByState(List<WritingPracticeCandidate> candidates,
                                                                    Instant now,
                                                                    String userId) {
        var grouped = new EnumMap<State, List<WritingPracticeCandidate>>(State.class);
        for (State state : State.values()) {
            grouped.put(state, new ArrayList<>());
        }
        for (var candidate : candidates) {
            grouped.computeIfAbsent(candidate.state(), key -> new ArrayList<>()).add(candidate);
        }
        for (var entry : grouped.entrySet()) {
            entry.getValue().sort(retrievabilityBiasedComparator(now));
            rotateBucket(entry.getValue(), userId, now, entry.getKey());
        }
        return grouped;
    }

    private void rotateBucket(List<WritingPracticeCandidate> bucket,
                              String userId,
                              Instant now,
                              State state) {
        if (bucket == null || bucket.size() <= 1) {
            return;
        }
        long timeSeed = now == null ? 0L : now.getEpochSecond();
        int seed = Objects.hash(userId == null ? "" : userId, timeSeed, state == null ? "NULL" : state.name());
        int offset = Math.floorMod(seed, bucket.size());
        if (offset == 0) {
            return;
        }
        var rotated = new ArrayList<WritingPracticeCandidate>(bucket.size());
        rotated.addAll(bucket.subList(offset, bucket.size()));
        rotated.addAll(bucket.subList(0, offset));
        bucket.clear();
        bucket.addAll(rotated);
    }

    private Comparator<WritingPracticeCandidate> retrievabilityBiasedComparator(Instant now) {
        return Comparator
                .comparing((WritingPracticeCandidate candidate) -> dueBucket(candidate, now))
                .thenComparing(WritingPracticeCandidate::retrievability, Comparator.reverseOrder())
                .thenComparing(WritingPracticeCandidate::lapses)
                .thenComparing(this::lastReviewOrMax, Comparator.reverseOrder())
                .thenComparing(candidate -> timeUntilDueOrMax(candidate, now))
                .thenComparingInt(candidate -> statePriority(candidate.state()))
                .thenComparing(WritingPracticeCandidate::vocabularyCreatedAt)
                .thenComparing(WritingPracticeCandidate::flashCardId);
    }

    private void addCandidates(List<WritingPracticeCandidate> selected,
                               List<WritingPracticeCandidate> candidates,
                               int targetCount) {
        if (candidates == null || candidates.isEmpty() || targetCount <= 0) {
            return;
        }
        for (var candidate : candidates) {
            if (selected.size() >= MAX_WORDS || targetCount <= 0) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            if (isFragile(candidate) && selectedFragileCount(selected) >= MAX_FRAGILE_CARDS) {
                continue;
            }
            selected.add(candidate);
            targetCount--;
        }
    }

    private int selectedFragileCount(List<WritingPracticeCandidate> selected) {
        return (int) selected.stream().filter(this::isFragile).count();
    }

    private boolean isFragile(WritingPracticeCandidate candidate) {
        return !Double.isNaN(candidate.retrievability())
                && candidate.retrievability() <= FRAGILE_RETRIEVABILITY_THRESHOLD;
    }

    private int dueBucket(WritingPracticeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return 1;
        }
        return candidate.due().isAfter(now) ? 1 : 0;
    }

    private java.time.Duration timeUntilDueOrMax(WritingPracticeCandidate candidate, Instant now) {
        if (candidate.due() == null) {
            return java.time.Duration.ofDays(36500);
        }
        return java.time.Duration.between(now, candidate.due()).abs();
    }

    private Instant lastReviewOrMax(WritingPracticeCandidate candidate) {
        return candidate.lastReview() == null ? Instant.MAX : candidate.lastReview();
    }

    private int statePriority(State state) {
        if (state == null) {
            return 3;
        }
        return switch (state) {
            case REVIEW -> 0;
            case LEARNING -> 1;
            case RE_LEARNING -> 2;
            case NEW -> 3;
        };
    }

    private Map<State, Integer> calculateRatioTargets(int count) {
        var rawTargets = new EnumMap<State, Double>(State.class);
        rawTargets.put(State.REVIEW, REVIEW_RATIO * count);
        rawTargets.put(State.LEARNING, LEARNING_RATIO * count);
        rawTargets.put(State.RE_LEARNING, RE_LEARNING_RATIO * count);

        var targets = new EnumMap<State, Integer>(State.class);
        int assigned = 0;
        for (var entry : rawTargets.entrySet()) {
            int base = (int) Math.floor(entry.getValue());
            targets.put(entry.getKey(), base);
            assigned += base;
        }

        int remaining = count - assigned;
        if (remaining <= 0) {
            return targets;
        }

        var byRemainder = rawTargets.entrySet().stream()
                .sorted(Comparator
                        .comparingDouble((Map.Entry<State, Double> entry) -> entry.getValue() - Math.floor(entry.getValue()))
                        .reversed()
                        .thenComparing(entry -> statePriority(entry.getKey())))
                .collect(Collectors.toList());

        int index = 0;
        while (remaining > 0 && index < byRemainder.size()) {
            var state = byRemainder.get(index).getKey();
            targets.put(state, targets.getOrDefault(state, 0) + 1);
            remaining--;
            index++;
            if (index >= byRemainder.size()) {
                index = 0;
            }
        }

        return targets;
    }
}
