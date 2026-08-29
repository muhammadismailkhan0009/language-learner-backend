package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReadingFlashcardReviewService {
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final ReviewVocabularyFlashcardApi reviewApi;
    private final PracticeVocabularyService practiceVocabularyService;
    private final ReadingPracticeRepo readingPracticeRepo;
    private final VocabularyExtractionService vocabularyExtractionService;

    public ReadingFlashcardReviewService(FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                         ReviewVocabularyFlashcardApi reviewApi,
                                         PracticeVocabularyService practiceVocabularyService) {
        this(flashcardReviewsApi, reviewApi, practiceVocabularyService, null, null);
    }

    @Autowired
    public ReadingFlashcardReviewService(FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                         ReviewVocabularyFlashcardApi reviewApi,
                                         PracticeVocabularyService practiceVocabularyService,
                                         ReadingPracticeRepo readingPracticeRepo,
                                         VocabularyExtractionService vocabularyExtractionService) {
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.reviewApi = reviewApi;
        this.practiceVocabularyService = practiceVocabularyService;
        this.readingPracticeRepo = readingPracticeRepo;
        this.vocabularyExtractionService = vocabularyExtractionService;
    }

    @Transactional
    public void review(String userId, String flashcardId, Rating rating) {
        reviewLegacy(userId, flashcardId, rating);
    }

    @Transactional
    public void review(String userId, String scenarioId, String flashcardId, Rating rating) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
        if (scenarioId == null || scenarioId.isBlank()) throw new IllegalArgumentException("scenarioId is required");
        if (flashcardId == null || flashcardId.isBlank()) throw new IllegalArgumentException("flashcardId is required");
        if (rating == null) throw new IllegalArgumentException("rating is required");

        var flashcard = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(value -> flashcardId.equals(value.flashcardId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary flashcard not found for user"));

        var scenario = readingPracticeRepo.findScenarioByIdAndUserIdForUpdate(scenarioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Reading scenario not found for user"));
        boolean attached = scenario.vocabularyUsages().stream()
                .anyMatch(usage -> flashcardId.equals(usage.flashCardId()));
        if (!attached) throw new IllegalArgumentException("Vocabulary flashcard is not attached to reading scenario");

        reviewApi.reviewVocabularyFlashcard(flashcardId, rating);
        var progress = scenario.recordAcceptedRating();
        if (!progress.scenario().equals(scenario)) {
            readingPracticeRepo.saveScenarioProgress(progress.scenario());
        }
        if (progress.completedNow()) {
            vocabularyExtractionService.submit(userId, scenario.readingText());
        }
        if (rating == Rating.GOOD || rating == Rating.EASY) {
            practiceVocabularyService.recordVocabularyMatch(userId, flashcard.vocabularyId());
        }
    }

    private void reviewLegacy(String userId, String flashcardId, Rating rating) {
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
