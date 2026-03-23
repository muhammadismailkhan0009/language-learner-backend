package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReadingPracticePolicy {

    private static final double VERY_WEAK_RETRIEVABILITY_THRESHOLD = 0.90;
    public static final int MAX_WORDS = 50;
    public static final double REVIEW_RATIO = 0.40;
    public static final double RE_LEARNING_RATIO = 0.30;
    public static final double LEARNING_RATIO = 0.20;
    public static final double NEW_RATIO = 0.10;
    public static final int MAX_VERY_WEAK_CARDS = 10;

    public List<ReadingPracticeCandidate> selectCandidates(String userId,
                                                           List<ReadingPracticeCandidate> candidates,
                                                           Instant rotationHour) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        var maxSelectable = Math.min(MAX_WORDS, candidates.size());

        var grouped = groupByState(candidates, rotationHour, userId);
        var selected = new ArrayList<ReadingPracticeCandidate>(maxSelectable);
        var targets = calculateRatioTargets(maxSelectable);

        addCandidates(selected, grouped.get(State.REVIEW), targets.getOrDefault(State.REVIEW, 0));
        addCandidates(selected, grouped.get(State.RE_LEARNING), targets.getOrDefault(State.RE_LEARNING, 0));
        addCandidates(selected, grouped.get(State.LEARNING), targets.getOrDefault(State.LEARNING, 0));
        addCandidates(selected, grouped.get(State.NEW), targets.getOrDefault(State.NEW, 0));

        return selected.subList(0, Math.min(selected.size(), maxSelectable));
    }

    private Map<State, List<ReadingPracticeCandidate>> groupByState(List<ReadingPracticeCandidate> candidates,
                                                                    Instant now,
                                                                    String userId) {
        var grouped = new EnumMap<State, List<ReadingPracticeCandidate>>(State.class);
        for (State state : State.values()) {
            grouped.put(state, new ArrayList<>());
        }
        for (var candidate : candidates) {
            grouped.computeIfAbsent(candidate.state(), ignored -> new ArrayList<>()).add(candidate);
        }
        for (var entry : grouped.entrySet()) {
            entry.getValue().sort(fsrsPriorityComparator(now));
            rotateBucket(entry.getValue(), userId, now, entry.getKey());
        }
        return grouped;
    }

    private void rotateBucket(List<ReadingPracticeCandidate> bucket,
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
        var rotated = new ArrayList<ReadingPracticeCandidate>(bucket.size());
        rotated.addAll(bucket.subList(offset, bucket.size()));
        rotated.addAll(bucket.subList(0, offset));
        bucket.clear();
        bucket.addAll(rotated);
    }

    private Comparator<ReadingPracticeCandidate> fsrsPriorityComparator(Instant now) {
        return Comparator
                .comparing((ReadingPracticeCandidate candidate) -> dueBucket(candidate, now))
                .thenComparing(candidate -> overdueDurationOrZero(candidate, now), Comparator.reverseOrder())
                .thenComparing(candidate -> timeUntilDueOrMax(candidate, now))
                .thenComparing(ReadingPracticeCandidate::retrievability)
                .thenComparing(this::lastReviewOrEpoch)
                .thenComparing(ReadingPracticeCandidate::lapses, Comparator.reverseOrder())
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
            if (isVeryWeak(candidate) && selectedVeryWeakCount(selected) >= MAX_VERY_WEAK_CARDS) {
                continue;
            }
            selected.add(candidate);
            targetCount--;
        }
    }

    private int selectedVeryWeakCount(List<ReadingPracticeCandidate> selected) {
        return (int) selected.stream().filter(this::isVeryWeak).count();
    }

    private boolean isVeryWeak(ReadingPracticeCandidate candidate) {
        return candidate.lapses() >= 2
                || (!Double.isNaN(candidate.retrievability())
                && candidate.retrievability() <= VERY_WEAK_RETRIEVABILITY_THRESHOLD);
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

    private Map<State, Integer> calculateRatioTargets(int count) {
        var rawTargets = new EnumMap<State, Double>(State.class);
        rawTargets.put(State.REVIEW, REVIEW_RATIO * count);
        rawTargets.put(State.RE_LEARNING, RE_LEARNING_RATIO * count);
        rawTargets.put(State.LEARNING, LEARNING_RATIO * count);
        rawTargets.put(State.NEW, NEW_RATIO * count);

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
