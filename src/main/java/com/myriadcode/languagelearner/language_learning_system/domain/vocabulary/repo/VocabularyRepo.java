package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepo {

    Vocabulary save(Vocabulary vocabulary);

    Optional<Vocabulary> findByIdAndUserId(String vocabularyId, String userId);

    List<Vocabulary> findByUserId(String userId);
}
