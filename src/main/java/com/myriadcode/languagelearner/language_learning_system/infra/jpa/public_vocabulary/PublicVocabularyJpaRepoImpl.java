package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.repo.PublicVocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.mappers.PublicVocabularyJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.repos.PublicVocabularyEntityJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PublicVocabularyJpaRepoImpl implements PublicVocabularyRepo {

    private static final PublicVocabularyJpaMapper PUBLIC_VOCABULARY_JPA_MAPPER = PublicVocabularyJpaMapper.INSTANCE;

    private final PublicVocabularyEntityJpaRepo publicVocabularyEntityJpaRepo;

    public PublicVocabularyJpaRepoImpl(PublicVocabularyEntityJpaRepo publicVocabularyEntityJpaRepo) {
        this.publicVocabularyEntityJpaRepo = publicVocabularyEntityJpaRepo;
    }

    @Override
    @Transactional
    public PublicVocabulary save(PublicVocabulary publicVocabulary) {
        var entity = PUBLIC_VOCABULARY_JPA_MAPPER.toEntity(publicVocabulary);
        var saved = publicVocabularyEntityJpaRepo.save(entity);
        return PUBLIC_VOCABULARY_JPA_MAPPER.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicVocabulary> findBySourceVocabularyId(String sourceVocabularyId) {
        return publicVocabularyEntityJpaRepo.findBySourceVocabularyId(sourceVocabularyId)
                .map(PUBLIC_VOCABULARY_JPA_MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublicVocabulary> findById(String publicVocabularyId) {
        return publicVocabularyEntityJpaRepo.findById(publicVocabularyId)
                .map(PUBLIC_VOCABULARY_JPA_MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicVocabulary> findAllByStatus(PublicVocabulary.PublicVocabularyStatus status) {
        return publicVocabularyEntityJpaRepo.findAllByStatusOrderByPublishedAtDesc(status.name()).stream()
                .map(PUBLIC_VOCABULARY_JPA_MAPPER::toDomain)
                .toList();
    }
}
