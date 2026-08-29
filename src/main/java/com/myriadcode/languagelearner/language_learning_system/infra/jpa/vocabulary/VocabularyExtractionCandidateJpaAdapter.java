package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionCandidateRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExtractionCandidateEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyExtractionCandidateJpaRepo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class VocabularyExtractionCandidateJpaAdapter implements VocabularyExtractionCandidateRepo {
    private final VocabularyExtractionCandidateJpaRepo repo;

    VocabularyExtractionCandidateJpaAdapter(VocabularyExtractionCandidateJpaRepo repo) {
        this.repo = repo;
    }

    @Override
    public List<VocabularyExtractionCandidate> saveAll(List<VocabularyExtractionCandidate> candidates) {
        var entities = candidates.stream().map(this::toEntity).toList();
        return repo.saveAll(entities).stream().map(this::toDomain).toList();
    }

    @Override
    public List<VocabularyExtractionCandidate> findByUserId(String userId) {
        return repo.findByUserIdOrderByCreatedAtAsc(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<VocabularyExtractionCandidate> findPendingByUserId(String userId) {
        return repo.findByUserIdAndCreatedVocabularyIdIsNullOrderByCreatedAtAsc(userId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsPendingByUserId(String userId) {
        return repo.existsByUserIdAndCreatedVocabularyIdIsNull(userId);
    }

    private VocabularyExtractionCandidateEntity toEntity(VocabularyExtractionCandidate value) {
        return new VocabularyExtractionCandidateEntity(
                value.id().id(), value.userId().id(), value.surface(),
                value.normalizedSurface(), value.createdVocabularyId(), value.createdAt());
    }

    private VocabularyExtractionCandidate toDomain(VocabularyExtractionCandidateEntity entity) {
        return new VocabularyExtractionCandidate(
                new VocabularyExtractionCandidate.VocabularyExtractionCandidateId(entity.getId()),
                new UserId(entity.getUserId()),
                entity.getSurface(), entity.getNormalizedSurface(), entity.getCreatedVocabularyId(), entity.getCreatedAt());
    }
}
