package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingFlashcardReviewService {
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final ReviewVocabularyFlashcardApi reviewApi;
    private final PracticeVocabularyService practiceVocabularyService;

    public ReadingFlashcardReviewService(FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                         ReviewVocabularyFlashcardApi reviewApi,
                                         PracticeVocabularyService practiceVocabularyService) {
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.reviewApi = reviewApi;
        this.practiceVocabularyService = practiceVocabularyService;
    }

    @Transactional
    public void review(String userId, String flashcardId, Rating rating) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        if (flashcardId == null || flashcardId.isBlank()) throw new IllegalArgumentException("flashcardId is required");
        if (rating == null) throw new IllegalArgumentException("rating is required");

        var flashcard = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(value -> flashcardId.equals(value.flashcardId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary flashcard not found for user"));

        reviewApi.reviewVocabularyFlashcard(flashcardId, rating);
        if (rating == Rating.GOOD || rating == Rating.EASY) {
            practiceVocabularyService.recordVocabularyMatch(userId, flashcard.vocabularyId());
        }
    }
}
