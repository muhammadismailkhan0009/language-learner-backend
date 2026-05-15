package com.myriadcode.languagelearner.language_learning_system.application.externals;

import com.myriadcode.fsrs.api.enums.Rating;

public interface ReviewVocabularyFlashcardApi {

    void reviewVocabularyFlashcard(String cardId, Rating rating);
}

