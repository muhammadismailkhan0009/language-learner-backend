package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WordPracticeTests {

    private final WordPractice englishToGermanPractice = new WordPractice(
            "practice-1", "user-1", "vocabulary-1", WordPracticeDirection.ENGLISH_TO_GERMAN,
            "I sign today.", "Ich _____ heute.", "Ich unterschreibe heute.",
            "unterschreibe", List.of("unterschreiben"), Instant.parse("2026-09-08T10:00:00Z")
    );

    private final WordPractice germanToEnglishPractice = new WordPractice(
            "practice-2", "user-1", "vocabulary-1", WordPracticeDirection.GERMAN_TO_ENGLISH,
            "Ich muss den Vertrag unterschreiben.", "I have to _____ the contract.",
            "I have to sign the contract.", "sign", List.of("sign the document"),
            Instant.parse("2026-09-08T10:01:00Z")
    );

    @Test
    void matches_english_to_german_exact_or_accepted_answer() {
        assertThat(englishToGermanPractice.isCorrect("  UNTERSCHREIBE ")).isTrue();
        assertThat(englishToGermanPractice.isCorrect("Unterschreiben")).isTrue();
    }

    @Test
    void matches_german_to_english_exact_or_accepted_answer() {
        assertThat(germanToEnglishPractice.isCorrect(" SIGN ")).isTrue();
        assertThat(germanToEnglishPractice.isCorrect("Sign the document")).isTrue();
    }

    @Test
    void rejects_fuzzy_or_missing_answer() {
        assertThat(englishToGermanPractice.isCorrect("unterschreib")).isFalse();
        assertThat(englishToGermanPractice.isCorrect("   ")).isFalse();
        assertThat(germanToEnglishPractice.isCorrect(null)).isFalse();
    }
}
