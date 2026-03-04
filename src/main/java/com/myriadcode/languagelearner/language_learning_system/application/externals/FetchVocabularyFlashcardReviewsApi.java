package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;

public interface FetchVocabularyFlashcardReviewsApi {

    List<VocabularyFlashcardReviewRecord> getVocabularyFlashcardsByUser(String userId);
}
