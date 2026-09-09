package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WordPracticeAnswerService {
    private final WordPracticeRepo practiceRepo;
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final ReviewVocabularyFlashcardApi reviewApi;

    public WordPracticeAnswerService(WordPracticeRepo practiceRepo,
                                     FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                     ReviewVocabularyFlashcardApi reviewApi) {
        this.practiceRepo = practiceRepo;
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.reviewApi = reviewApi;
    }

    @Transactional
    public boolean submit(String userId, String practiceId, String answer) {
        requireNonblank(userId, "userId");
        requireNonblank(practiceId, "practiceId");
        var practice = practiceRepo.findByIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Word Practice not found for user"));
        var flashcard = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(value -> value.isReversed() && practice.vocabularyId().equals(value.vocabularyId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary flashcard not found for user"));
        boolean correct = practice.isCorrect(answer);
        reviewApi.reviewVocabularyFlashcard(flashcard.flashcardId(), correct ? Rating.HARD : Rating.AGAIN);
        if (correct) {
            practiceRepo.deleteByIdAndUserId(practiceId, userId);
        }
        return correct;
    }

    private void requireNonblank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
