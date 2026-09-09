package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InsufficientWordPracticeCandidatesException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeSelectionPolicyTests {

    private final WordPracticeSelectionPolicy policy = new WordPracticeSelectionPolicy();

    @Test
    void selects_seven_learning_and_three_new_words() {
        var windows = windows(
                candidates(WordPracticeSelectionCategory.LEARNING, "learning", 10),
                candidates(WordPracticeSelectionCategory.NEW, "new", 10)
        );

        var selected = policy.select(windows, Set.of(), 10, new Random(7));

        assertThat(selected).hasSize(10);
        assertThat(selected).extracting(WordPracticeCandidate::vocabularyId).doesNotHaveDuplicates();
        assertThat(selected).filteredOn(candidate -> candidate.category() == WordPracticeSelectionCategory.LEARNING).hasSize(7);
        assertThat(selected).filteredOn(candidate -> candidate.category() == WordPracticeSelectionCategory.NEW).hasSize(3);
    }

    @Test
    void builds_candidate_window_randomly_instead_of_always_using_first_twenty() {
        var rankedNew = candidates(WordPracticeSelectionCategory.NEW, "new", 100);
        var windows = windows(rankedNew);

        var selected = policy.select(windows, Set.of(), 10, new Random(11));

        assertThat(selected)
                .filteredOn(candidate -> candidate.category() == WordPracticeSelectionCategory.NEW)
                .extracting(WordPracticeCandidate::vocabularyId)
                .anyMatch(id -> Integer.parseInt(id.substring(id.lastIndexOf('-') + 1)) > 20);
    }

    @Test
    void accepts_one_eligible_word() {
        var windows = windows(
                candidates(WordPracticeSelectionCategory.NEW, "new", 1)
        );

        assertThat(policy.select(windows, Set.of(), 10, new Random(1)))
                .singleElement()
                .extracting(WordPracticeCandidate::vocabularyId)
                .isEqualTo("new-1");
    }

    @Test
    void rejects_when_no_eligible_word_exists() {
        assertThatThrownBy(() -> policy.select(Map.of(), Set.of(), 10, new Random(1)))
                .isInstanceOf(InsufficientWordPracticeCandidatesException.class)
                .extracting("availableVocabularyCount", "requiredVocabularyCount")
                .containsExactly(0, 1);
    }

    @SafeVarargs
    private Map<WordPracticeSelectionCategory, List<WordPracticeCandidate>> windows(
            List<WordPracticeCandidate>... lists
    ) {
        var windows = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeCandidate>>(
                WordPracticeSelectionCategory.class
        );
        for (var list : lists) {
            if (!list.isEmpty()) {
                windows.put(list.getFirst().category(), list);
            }
        }
        return windows;
    }

    private List<WordPracticeCandidate> candidates(WordPracticeSelectionCategory category, String prefix, int count) {
        var candidates = new ArrayList<WordPracticeCandidate>();
        for (int index = 1; index <= count; index++) {
            candidates.add(new WordPracticeCandidate(prefix + "-" + index, category));
        }
        return List.copyOf(candidates);
    }
}
