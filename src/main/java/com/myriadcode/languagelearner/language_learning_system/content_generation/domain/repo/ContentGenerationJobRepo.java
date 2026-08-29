package com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;

import java.util.Optional;

public interface ContentGenerationJobRepo {
    ContentGenerationJob save(ContentGenerationJob job);
    Optional<ContentGenerationJob> findByUserIdAndType(String userId, ContentGenerationJobType type);
    void deleteByUserIdAndType(String userId, ContentGenerationJobType type);
}
