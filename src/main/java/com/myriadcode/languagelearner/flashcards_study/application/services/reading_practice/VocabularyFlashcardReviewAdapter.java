package com.myriadcode.languagelearner.flashcards_study.application.services.reading_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import org.springframework.stereotype.Service;

@Service
public class VocabularyFlashcardReviewAdapter implements ReviewVocabularyFlashcardApi {

    private final CardStudyService cardStudyService;

    public VocabularyFlashcardReviewAdapter(CardStudyService cardStudyService) {
        this.cardStudyService = cardStudyService;
    }

    @Override
    public void reviewVocabularyFlashcard(String cardId, Rating rating) {
        cardStudyService.reviewVocabularyStudiedCard(cardId, rating);
    }
}

