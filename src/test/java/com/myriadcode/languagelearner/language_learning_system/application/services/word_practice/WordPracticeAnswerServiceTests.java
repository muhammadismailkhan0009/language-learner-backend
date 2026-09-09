package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
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
    private final RecordingReviewApi reviewApi = new RecordingReviewApi();
    private final WordPracticeAnswerService service = new WordPracticeAnswerService(
            repo,
            userId -> List.of(
                    new VocabularyFlashcardReviewRecord("other-flashcard", "other-vocabulary", State.NEW, true),
                    new VocabularyFlashcardReviewRecord("forward-flashcard", "vocabulary-1", State.NEW, false),
                    new VocabularyFlashcardReviewRecord("flashcard-1", "vocabulary-1", State.NEW, true)
            ),
            reviewApi
    );

    @Test
    void correctAnswerDeletesPractice() {
        assertThat(service.submit("user-1", "practice-1", " unterschreiben ")).isTrue();
        assertThat(repo.deleted).isTrue();
        assertThat(reviewApi.flashcardId).isEqualTo("flashcard-1");
        assertThat(reviewApi.rating).isEqualTo(Rating.HARD);
    }

    @Test
    void wrongAnswerKeepsPractice() {
        assertThat(service.submit("user-1", "practice-1", "zeichnen")).isFalse();
        assertThat(repo.deleted).isFalse();
        assertThat(reviewApi.flashcardId).isEqualTo("flashcard-1");
        assertThat(reviewApi.rating).isEqualTo(Rating.AGAIN);
    }

    private static final class RecordingReviewApi implements ReviewVocabularyFlashcardApi {
        private String flashcardId;
        private Rating rating;

        @Override
        public void reviewVocabularyFlashcard(String flashcardId, Rating rating) {
            this.flashcardId = flashcardId;
            this.rating = rating;
        }
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
        @Override public void saveGeneration(String userId, List<GeneratedWordPracticeGroup> groups, Instant createdAt) { }
    }
}
