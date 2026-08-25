package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.ContentGenerationJobRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class ContentGenerationJobJpaAdapter implements ContentGenerationJobRepo {
    private final ContentGenerationJobJpaRepo jpaRepo;

    ContentGenerationJobJpaAdapter(ContentGenerationJobJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public ContentGenerationJob save(ContentGenerationJob job) {
        var saved = jpaRepo.save(new ContentGenerationJobEntity(job.userId(), job.type(), job.createdAt()));
        return toDomain(saved);
    }

    @Override
    public Optional<ContentGenerationJob> findByUserId(String userId) {
        return jpaRepo.findById(userId).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaRepo.deleteById(userId);
    }

    private ContentGenerationJob toDomain(ContentGenerationJobEntity entity) {
        return new ContentGenerationJob(entity.getUserId(), entity.getType(), entity.getCreatedAt());
    }
}
