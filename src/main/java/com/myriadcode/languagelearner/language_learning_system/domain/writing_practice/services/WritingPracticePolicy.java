package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services;

import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WritingPracticePolicy {

    public static final int MAX_WORDS = 20;
    public static final int REVIEW_COUNT = 6;
    public static final int RE_LEARNING_COUNT = 8;
    public static final int LEARNING_COUNT = 4;
    public static final int NEW_COUNT = 2;

    public List<WritingPracticeCandidate> selectCandidates(String userId,
                                                           List<WritingPracticeCandidate> candidates,
                                                           Instant rotationHour) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        var grouped = groupByState(candidates);
        var selected = new ArrayList<WritingPracticeCandidate>();

        selected.addAll(selectFromState(userId, rotationHour, grouped.get(State.REVIEW), REVIEW_COUNT));
        selected.addAll(selectFromState(userId, rotationHour, grouped.get(State.RE_LEARNING), RE_LEARNING_COUNT));
        selected.addAll(selectFromState(userId, rotationHour, grouped.get(State.LEARNING), LEARNING_COUNT));
        selected.addAll(selectFromState(userId, rotationHour, grouped.get(State.NEW), NEW_COUNT));

        if (selected.size() >= MAX_WORDS) {
            return selected.subList(0, MAX_WORDS);
        }

        var remaining = candidates.stream()
                .filter(candidate -> !selected.contains(candidate))
                .sorted(Comparator.comparing(WritingPracticeCandidate::vocabularyCreatedAt))
                .toList();

        var needed = MAX_WORDS - selected.size();
        if (remaining.isEmpty() || needed <= 0) {
            return selected;
        }

        selected.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        return selected;
    }

    private Map<State, List<WritingPracticeCandidate>> groupByState(List<WritingPracticeCandidate> candidates) {
        var grouped = new EnumMap<State, List<WritingPracticeCandidate>>(State.class);
        for (State state : State.values()) {
            grouped.put(state, new ArrayList<>());
        }
        for (var candidate : candidates) {
            grouped.computeIfAbsent(candidate.state(), key -> new ArrayList<>()).add(candidate);
        }
        for (var entry : grouped.entrySet()) {
            entry.getValue().sort(Comparator.comparing(WritingPracticeCandidate::vocabularyCreatedAt));
        }
        return grouped;
    }

    private List<WritingPracticeCandidate> selectFromState(String userId,
                                                           Instant rotationHour,
                                                           List<WritingPracticeCandidate> candidates,
                                                           int count) {
        if (candidates == null || candidates.isEmpty() || count <= 0) {
            return List.of();
        }
        var size = candidates.size();
        var windowSize = Math.min(size, count * 3);
        var maxStart = Math.max(1, size - windowSize + 1);
        var start = Math.floorMod(Objects.hash(userId, rotationHour.toString(), candidates.getFirst().state().name()),
                maxStart);
        var window = candidates.subList(start, start + windowSize);
        return window.subList(0, Math.min(count, window.size()));
    }
}
