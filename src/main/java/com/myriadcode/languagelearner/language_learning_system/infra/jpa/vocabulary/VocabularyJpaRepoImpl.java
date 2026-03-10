package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers.VocabularyJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class VocabularyJpaRepoImpl implements VocabularyRepo {

    private static final VocabularyJpaMapper VOCABULARY_JPA_MAPPER = VocabularyJpaMapper.INSTANCE;
    private final VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    public VocabularyJpaRepoImpl(VocabularyEntityJpaRepo vocabularyEntityJpaRepo) {
        this.vocabularyEntityJpaRepo = vocabularyEntityJpaRepo;
    }

    @Override
    @Transactional
    public Vocabulary save(Vocabulary vocabulary) {
        var entity = VOCABULARY_JPA_MAPPER.toEntity(vocabulary);
        entity.setSchemaVersion(1);
        for (int i = 0; i < entity.getExampleSentences().size(); i++) {
            entity.getExampleSentences().get(i).setDisplayOrder(i);
        }
        if (entity.getClozeSentence() != null && entity.getClozeSentence().getCreatedAt() == null) {
            entity.getClozeSentence().setCreatedAt(java.time.Instant.now());
        }
        var saved = vocabularyEntityJpaRepo.save(entity);
        return VOCABULARY_JPA_MAPPER.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vocabulary> findByIdAndUserId(String vocabularyId, String userId) {
        return vocabularyEntityJpaRepo.findByIdAndUserId(vocabularyId, userId)
                .map(VOCABULARY_JPA_MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vocabulary> findById(String vocabularyId) {
        return vocabularyEntityJpaRepo.findById(vocabularyId)
                .map(VOCABULARY_JPA_MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vocabulary> findByUserId(String userId) {
        return vocabularyEntityJpaRepo.findAllByUserId(userId).stream()
                .map(VOCABULARY_JPA_MAPPER::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vocabulary> findByIds(List<String> vocabularyIds) {
        if (vocabularyIds == null || vocabularyIds.isEmpty()) {
            return List.of();
        }
        return vocabularyEntityJpaRepo.findAllByIdIn(vocabularyIds).stream()
                .map(VOCABULARY_JPA_MAPPER::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Vocabulary replaceClozeSentence(String vocabularyId, String userId, Vocabulary vocabularyWithUpdatedCloze) {
        var entity = vocabularyEntityJpaRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        entity.setClozeSentence(vocabularyWithUpdatedCloze.clozeSentence() == null
                ? null
                : VOCABULARY_JPA_MAPPER.toClozeSentenceEntity(vocabularyWithUpdatedCloze.clozeSentence()));
        if (entity.getClozeSentence() != null && entity.getClozeSentence().getCreatedAt() == null) {
            entity.getClozeSentence().setCreatedAt(java.time.Instant.now());
        }
        return VOCABULARY_JPA_MAPPER.toDomain(vocabularyEntityJpaRepo.save(entity));
    }
}
