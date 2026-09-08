package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.WordPracticeCapacityExceededException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeCapacityPolicyTests {

    private final WordPracticeCapacityPolicy policy = new WordPracticeCapacityPolicy();

    @Test
    void allows_ten_new_words_when_five_words_are_active() {
        assertThatCode(() -> policy.requireCapacity(5, 10)).doesNotThrowAnyException();
    }

    @Test
    void rejects_ten_new_words_when_six_words_are_active() {
        assertThatThrownBy(() -> policy.requireCapacity(6, 10))
                .isInstanceOf(WordPracticeCapacityExceededException.class)
                .extracting("activeVocabularyCount", "requestedVocabularyCount", "maximumVocabularyCount")
                .containsExactly(6, 10, 15);
    }
}
