package com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;

import java.util.List;
import java.util.Optional;

public interface PracticeVocabularyReferenceRepo {

    PracticeVocabularyReference save(PracticeVocabularyReference reference);

    Optional<PracticeVocabularyReference> findByUserIdAndVocabularyId(String userId, String vocabularyId);

    List<PracticeVocabularyReference> findByUserId(String userId);
}
