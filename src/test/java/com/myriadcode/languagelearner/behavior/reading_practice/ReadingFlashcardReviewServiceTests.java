package com.myriadcode.languagelearner.behavior.reading_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice.ReadingFlashcardReviewService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReadingFlashcardReviewServiceTests {
    private final FetchVocabularyFlashcardReviewsApi reviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
    private final ReviewVocabularyFlashcardApi reviewApi = mock(ReviewVocabularyFlashcardApi.class);
    private final PracticeVocabularyService practiceVocabularyService = mock(PracticeVocabularyService.class);
    private final ReadingFlashcardReviewService service = new ReadingFlashcardReviewService(
            reviewsApi, reviewApi, practiceVocabularyService);

    @Test
    void goodRatingReviewsCardAndAddsVocabularyToWritingPool() {
        stubCard();

        service.review("user-1", "card-1", Rating.GOOD);

        verify(reviewApi).reviewVocabularyFlashcard("card-1", Rating.GOOD);
        verify(practiceVocabularyService).recordVocabularyMatch("user-1", "vocab-1");
    }

    @Test
    void easyRatingAddsVocabularyToWritingPool() {
        stubCard();

        service.review("user-1", "card-1", Rating.EASY);

        verify(practiceVocabularyService).recordVocabularyMatch("user-1", "vocab-1");
    }

    @Test
    void hardRatingDoesNotAddVocabularyToWritingPool() {
        stubCard();

        service.review("user-1", "card-1", Rating.HARD);

        verify(reviewApi).reviewVocabularyFlashcard("card-1", Rating.HARD);
        verifyNoInteractions(practiceVocabularyService);
    }

    @Test
    void rejectsFlashcardThatDoesNotBelongToUserBeforeReviewing() {
        when(reviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of());

        assertThatThrownBy(() -> service.review("user-1", "card-1", Rating.GOOD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found for user");

        verifyNoInteractions(reviewApi, practiceVocabularyService);
    }

    private void stubCard() {
        when(reviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("card-1", "vocab-1", State.REVIEW, true)));
    }
}
