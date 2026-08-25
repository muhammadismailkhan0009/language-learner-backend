package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.McpCredential;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.McpCredentialRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class McpCredentialJpaAdapter implements McpCredentialRepo {
    private final McpCredentialJpaRepo jpaRepo;

    McpCredentialJpaAdapter(McpCredentialJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public McpCredential save(McpCredential credential) {
        return toDomain(jpaRepo.save(new McpCredentialEntity(
                credential.userId(), credential.secretKey(), credential.createdAt())));
    }

    @Override
    public Optional<McpCredential> findByUserId(String userId) {
        return jpaRepo.findById(userId).map(this::toDomain);
    }

    @Override
    public Optional<McpCredential> findBySecretKey(String secretKey) {
        return jpaRepo.findBySecretKey(secretKey).map(this::toDomain);
    }

    private McpCredential toDomain(McpCredentialEntity entity) {
        return new McpCredential(entity.getUserId(), entity.getSecretKey(), entity.getCreatedAt());
    }
}
