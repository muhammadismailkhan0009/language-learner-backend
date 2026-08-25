package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;

import java.util.Optional;

public interface ContentGenerationJobRepo {
    ContentGenerationJob save(ContentGenerationJob job);
    Optional<ContentGenerationJob> findByUserId(String userId);
    void deleteByUserId(String userId);
}
