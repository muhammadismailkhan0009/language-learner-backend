package com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;

import java.util.List;
import java.util.Optional;

public interface PublicVocabularyRepo {

    PublicVocabulary save(PublicVocabulary publicVocabulary);

    Optional<PublicVocabulary> findBySourceVocabularyId(String sourceVocabularyId);

    List<PublicVocabulary> findAllByStatus(PublicVocabulary.PublicVocabularyStatus status);
}
