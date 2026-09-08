package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InsufficientWordPracticeCandidatesException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.random.RandomGenerator;

public class WordPracticeSelectionPolicy {

    public static final int BATCH_SIZE = 10;
    public static final int CANDIDATE_WINDOW_SIZE = 20;

    private static final Map<WordPracticeSelectionCategory, Integer> QUOTAS = Map.of(
            WordPracticeSelectionCategory.NEW, 5,
            WordPracticeSelectionCategory.WEAK, 3,
            WordPracticeSelectionCategory.LOW_EXPOSURE, 2
    );
    private static final List<WordPracticeSelectionCategory> SELECTION_ORDER = List.of(
            WordPracticeSelectionCategory.NEW,
            WordPracticeSelectionCategory.WEAK,
            WordPracticeSelectionCategory.LOW_EXPOSURE,
            WordPracticeSelectionCategory.STALE,
            WordPracticeSelectionCategory.RANDOM
    );

    public List<WordPracticeCandidate> select(
            Map<WordPracticeSelectionCategory, List<WordPracticeCandidate>> rankedCandidates,
            Set<String> activeVocabularyIds,
            RandomGenerator random
    ) {
        Objects.requireNonNull(random, "random must not be null");
        var activeIds = activeVocabularyIds == null ? Set.<String>of() : Set.copyOf(activeVocabularyIds);
        var windows = createEligibleWindows(rankedCandidates, activeIds);
        int availableCount = (int) windows.values().stream()
                .flatMap(List::stream)
                .map(WordPracticeCandidate::vocabularyId)
                .distinct()
                .count();
        if (availableCount < BATCH_SIZE) {
            throw new InsufficientWordPracticeCandidatesException(availableCount, BATCH_SIZE);
        }

        var selected = new ArrayList<WordPracticeCandidate>(BATCH_SIZE);
        var selectedIds = new HashSet<String>();
        for (var category : List.of(
                WordPracticeSelectionCategory.NEW,
                WordPracticeSelectionCategory.WEAK,
                WordPracticeSelectionCategory.LOW_EXPOSURE)) {
            addRandom(selected, selectedIds, windows.get(category), QUOTAS.get(category), random);
        }
        for (var category : SELECTION_ORDER) {
            addRandom(selected, selectedIds, windows.get(category), BATCH_SIZE - selected.size(), random);
        }
        return List.copyOf(selected);
    }

    private Map<WordPracticeSelectionCategory, List<WordPracticeCandidate>> createEligibleWindows(
            Map<WordPracticeSelectionCategory, List<WordPracticeCandidate>> rankedCandidates,
            Set<String> activeIds
    ) {
        var windows = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeCandidate>>(
                WordPracticeSelectionCategory.class
        );
        var source = rankedCandidates == null
                ? Map.<WordPracticeSelectionCategory, List<WordPracticeCandidate>>of()
                : rankedCandidates;
        for (var category : SELECTION_ORDER) {
            var window = source.getOrDefault(category, List.of()).stream()
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.category() == category)
                    .filter(candidate -> !activeIds.contains(candidate.vocabularyId()))
                    .limit(CANDIDATE_WINDOW_SIZE)
                    .toList();
            windows.put(category, window);
        }
        return windows;
    }

    private void addRandom(List<WordPracticeCandidate> selected,
                           Set<String> selectedIds,
                           List<WordPracticeCandidate> candidates,
                           int requested,
                           RandomGenerator random) {
        if (requested <= 0 || candidates == null || candidates.isEmpty()) {
            return;
        }
        var available = new ArrayList<>(candidates.stream()
                .filter(candidate -> !selectedIds.contains(candidate.vocabularyId()))
                .toList());
        int added = 0;
        while (added < requested && !available.isEmpty() && selected.size() < BATCH_SIZE) {
            var candidate = available.remove(random.nextInt(available.size()));
            if (selectedIds.add(candidate.vocabularyId())) {
                selected.add(candidate);
                added++;
            }
        }
    }
}
