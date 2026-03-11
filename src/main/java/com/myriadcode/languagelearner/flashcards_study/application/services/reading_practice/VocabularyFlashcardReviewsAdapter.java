package com.myriadcode.languagelearner.flashcards_study.application.services.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyFlashcardReviewsAdapter implements FetchVocabularyFlashcardReviewsApi {

    private final FlashCardRepo flashCardRepo;

    public VocabularyFlashcardReviewsAdapter(FlashCardRepo flashCardRepo) {
        this.flashCardRepo = flashCardRepo;
    }

    @Override
    public List<VocabularyFlashcardReviewRecord> getVocabularyFlashcardsByUser(String userId) {
        var cards = flashCardRepo.findVocabularyFlashCardsByUser(userId);
        return cards.stream()
                .map(review -> new VocabularyFlashcardReviewRecord(
                        review.id().id(),
                        review.contentId().id(),
                        review.cardReviewData().state(),
                        review.cardReviewData().due(),
                        review.cardReviewData().stability(),
                        review.cardReviewData().difficulty(),
                        review.cardReviewData().lapses(),
                        review.cardReviewData().lastReview(),
                        review.isReversed()
                ))
                .toList();
    }
}
