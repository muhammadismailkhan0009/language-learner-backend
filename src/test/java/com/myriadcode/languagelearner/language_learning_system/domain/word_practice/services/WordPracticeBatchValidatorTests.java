package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InvalidWordPracticeBatchException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeBatchValidatorTests {

    private final WordPracticeBatchValidator validator = new WordPracticeBatchValidator();

    @Test
    void accepts_ten_authoritative_words_with_three_to_five_distinct_contexts_and_both_directions() {
        var vocabularyIds = ids(10);

        assertThatCode(() -> validator.validate(vocabularyIds, groups(vocabularyIds, 3)))
                .doesNotThrowAnyException();
    }

    @Test
    void rejects_missing_or_replaced_authoritative_vocabulary() {
        var vocabularyIds = ids(10);
        var groups = new ArrayList<>(groups(vocabularyIds, 3));
        groups.set(9, group("replacement", 3));

        assertThatThrownBy(() -> validator.validate(vocabularyIds, groups))
                .isInstanceOf(InvalidWordPracticeBatchException.class);
    }

    @Test
    void rejects_word_with_fewer_than_three_practices() {
        var vocabularyIds = ids(10);
        var groups = new ArrayList<>(groups(vocabularyIds, 3));
        groups.set(0, group("vocabulary-1", 2));

        assertThatThrownBy(() -> validator.validate(vocabularyIds, groups))
                .isInstanceOf(InvalidWordPracticeBatchException.class);
    }

    @Test
    void rejects_duplicate_contexts_for_same_word() {
        var vocabularyIds = ids(10);
        var groups = new ArrayList<>(groups(vocabularyIds, 3));
        var duplicate = practice("vocabulary-1", 1, WordPracticeDirection.GERMAN_TO_ENGLISH);
        groups.set(0, new GeneratedWordPracticeGroup("vocabulary-1", List.of(duplicate, duplicate, practice(
                "vocabulary-1", 2, WordPracticeDirection.ENGLISH_TO_GERMAN))));

        assertThatThrownBy(() -> validator.validate(vocabularyIds, groups))
                .isInstanceOf(InvalidWordPracticeBatchException.class);
    }

    @Test
    void rejects_batch_containing_only_one_direction() {
        var vocabularyIds = ids(10);
        var groups = vocabularyIds.stream()
                .map(id -> new GeneratedWordPracticeGroup(id, List.of(
                        practice(id, 1, WordPracticeDirection.GERMAN_TO_ENGLISH),
                        practice(id, 2, WordPracticeDirection.GERMAN_TO_ENGLISH),
                        practice(id, 3, WordPracticeDirection.GERMAN_TO_ENGLISH))))
                .toList();

        assertThatThrownBy(() -> validator.validate(vocabularyIds, groups))
                .isInstanceOf(InvalidWordPracticeBatchException.class);
    }

    private List<String> ids(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(index -> "vocabulary-" + index)
                .toList();
    }

    private List<GeneratedWordPracticeGroup> groups(List<String> vocabularyIds, int practicesPerWord) {
        return vocabularyIds.stream().map(id -> group(id, practicesPerWord)).toList();
    }

    private GeneratedWordPracticeGroup group(String vocabularyId, int count) {
        var practices = new ArrayList<GeneratedWordPractice>();
        for (int index = 1; index <= count; index++) {
            practices.add(practice(vocabularyId, index,
                    index % 2 == 0 ? WordPracticeDirection.ENGLISH_TO_GERMAN : WordPracticeDirection.GERMAN_TO_ENGLISH));
        }
        return new GeneratedWordPracticeGroup(vocabularyId, List.copyOf(practices));
    }

    private GeneratedWordPractice practice(String vocabularyId, int context, WordPracticeDirection direction) {
        return new GeneratedWordPractice(
                vocabularyId,
                direction,
                "Source " + context,
                "Cloze _____ " + context,
                "Complete " + context,
                "Answer " + context,
                List.of("Alternative " + context)
        );
    }
}
