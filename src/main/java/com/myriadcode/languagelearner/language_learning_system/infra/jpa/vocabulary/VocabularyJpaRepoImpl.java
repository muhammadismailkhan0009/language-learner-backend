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
    public List<Vocabulary> findByUserId(String userId) {
        return vocabularyEntityJpaRepo.findAllByUserId(userId).stream()
                .map(VOCABULARY_JPA_MAPPER::toDomain)
                .toList();
    }
}
