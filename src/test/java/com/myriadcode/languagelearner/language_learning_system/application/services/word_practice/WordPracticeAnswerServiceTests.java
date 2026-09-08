package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WordPracticeAnswerServiceTests {
    private final RecordingRepo repo = new RecordingRepo();
    private final WordPracticeAnswerService service = new WordPracticeAnswerService(repo);

    @Test
    void correctAnswerDeletesPractice() {
        assertThat(service.submit("user-1", "practice-1", " unterschreiben ")).isTrue();
        assertThat(repo.deleted).isTrue();
    }

    @Test
    void wrongAnswerKeepsPractice() {
        assertThat(service.submit("user-1", "practice-1", "zeichnen")).isFalse();
        assertThat(repo.deleted).isFalse();
    }

    private static final class RecordingRepo implements WordPracticeRepo {
        private boolean deleted;
        private final WordPractice practice = new WordPractice(
                "practice-1", "user-1", "vocabulary-1", WordPracticeDirection.ENGLISH_TO_GERMAN,
                "Please sign here.", "Bitte _____ Sie hier.", "Bitte unterschreiben Sie hier.",
                "unterschreiben", List.of(), Instant.EPOCH
        );

        @Override public int countDistinctActiveVocabulary(String userId) { return 1; }
        @Override public Set<String> findActiveVocabularyIds(String userId) { return Set.of("vocabulary-1"); }
        @Override public List<WordPractice> findByUserId(String userId) { return List.of(practice); }
        @Override public Optional<WordPractice> findByIdAndUserId(String practiceId, String userId) { return Optional.of(practice); }
        @Override public void deleteByIdAndUserId(String practiceId, String userId) { deleted = true; }
        @Override public void saveGenerationAndConsumeWeakEvents(String userId, List<WordPracticeCandidate> selected, List<GeneratedWordPracticeGroup> groups, Instant createdAt) { }
    }
}
