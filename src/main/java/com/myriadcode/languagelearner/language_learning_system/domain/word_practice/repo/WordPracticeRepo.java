package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WordPracticeRepo {
    int countDistinctActiveVocabulary(String userId);

    Set<String> findActiveVocabularyIds(String userId);

    List<WordPractice> findByUserId(String userId);

    Optional<WordPractice> findByIdAndUserId(String practiceId, String userId);

    void deleteByIdAndUserId(String practiceId, String userId);

    void saveGeneration(String userId, List<GeneratedWordPracticeGroup> groups, Instant createdAt);
}
