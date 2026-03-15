package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class WritingPracticePolicy {

    private static final double FRAGILE_RETRIEVABILITY_THRESHOLD = 0.90;
    public static final int MAX_WORDS = 20;
    public static final int REVIEW_COUNT = 15;
    public static final int LEARNING_COUNT = 3;
    public static final int RE_LEARNING_COUNT = 2;
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

        var grouped = groupByState(eligible, rotationHour);
        var selected = new ArrayList<WritingPracticeCandidate>(Math.min(MAX_WORDS, eligible.size()));

        addCandidates(selected, grouped.get(State.REVIEW), REVIEW_COUNT);
        addCandidates(selected, grouped.get(State.LEARNING), LEARNING_COUNT);
        addCandidates(selected, grouped.get(State.RE_LEARNING), RE_LEARNING_COUNT);

        var remainder = eligible.stream()
                .filter(candidate -> !selected.contains(candidate))
                .sorted(retrievabilityBiasedComparator(rotationHour))
                .toList();
        addCandidates(selected, remainder, Math.min(MAX_WORDS, eligible.size()) - selected.size());
        addRemainingCandidates(selected, remainder, Math.min(MAX_WORDS, eligible.size()) - selected.size());

        return selected.subList(0, Math.min(selected.size(), Math.min(MAX_WORDS, eligible.size())));
    }

    private Map<State, List<WritingPracticeCandidate>> groupByState(List<WritingPracticeCandidate> candidates,
                                                                    Instant now) {
        var grouped = new EnumMap<State, List<WritingPracticeCandidate>>(State.class);
        for (State state : State.values()) {
            grouped.put(state, new ArrayList<>());
        }
        for (var candidate : candidates) {
            grouped.computeIfAbsent(candidate.state(), key -> new ArrayList<>()).add(candidate);
        }
        for (var entry : grouped.entrySet()) {
            entry.getValue().sort(retrievabilityBiasedComparator(now));
        }
        return grouped;
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

    private void addRemainingCandidates(List<WritingPracticeCandidate> selected,
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
            selected.add(candidate);
            targetCount--;
        }
    }

    private boolean isFragile(WritingPracticeCandidate candidate) {
        return candidate.lapses() >= 2
                || (!Double.isNaN(candidate.retrievability())
                && candidate.retrievability() <= FRAGILE_RETRIEVABILITY_THRESHOLD);
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
}
