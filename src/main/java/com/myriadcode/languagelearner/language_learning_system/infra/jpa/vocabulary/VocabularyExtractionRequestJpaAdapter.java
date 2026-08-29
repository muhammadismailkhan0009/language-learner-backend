package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExtractionRequestEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyExtractionRequestJpaRepo;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class VocabularyExtractionRequestJpaAdapter implements VocabularyExtractionRequestRepo {
    private final VocabularyExtractionRequestJpaRepo repo;

    VocabularyExtractionRequestJpaAdapter(VocabularyExtractionRequestJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public VocabularyExtractionRequest save(VocabularyExtractionRequest request) {
        return toDomain(repo.save(new VocabularyExtractionRequestEntity(
                request.id().id(), request.userId().id(), request.sourceText(), request.createdAt())));
    }

    @Override
    public Optional<VocabularyExtractionRequest> findOldestByUserId(String userId) {
        return repo.findFirstByUserIdOrderByCreatedAtAsc(userId).map(this::toDomain);
    }

    @Override
    public Optional<VocabularyExtractionRequest> findByIdAndUserId(String requestId, String userId) {
        return repo.findByIdAndUserId(requestId, userId).map(this::toDomain);
    }

    @Override
    public boolean existsByUserId(String userId) { return repo.existsByUserId(userId); }

    @Override
    public void delete(VocabularyExtractionRequest.VocabularyExtractionRequestId id) {
        repo.deleteById(id.id());
    }

    private VocabularyExtractionRequest toDomain(VocabularyExtractionRequestEntity entity) {
        return new VocabularyExtractionRequest(
                new VocabularyExtractionRequest.VocabularyExtractionRequestId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getSourceText(),
                entity.getCreatedAt());
    }
}
