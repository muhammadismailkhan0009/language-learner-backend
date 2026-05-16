package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.mappers.PracticeVocabularyReferenceJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.repos.PracticeVocabularyReferenceEntityJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PracticeVocabularyReferenceJpaRepoImpl implements PracticeVocabularyReferenceRepo {

    private static final PracticeVocabularyReferenceJpaMapper MAPPER = PracticeVocabularyReferenceJpaMapper.INSTANCE;

    private final PracticeVocabularyReferenceEntityJpaRepo jpaRepo;

    public PracticeVocabularyReferenceJpaRepoImpl(PracticeVocabularyReferenceEntityJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public PracticeVocabularyReference save(PracticeVocabularyReference reference) {
        var entity = MAPPER.toEntity(reference);
        var saved = jpaRepo.save(entity);
        return MAPPER.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PracticeVocabularyReference> findByUserIdAndVocabularyId(String userId, String vocabularyId) {
        return jpaRepo.findByUserIdAndVocabularyId(userId, vocabularyId).map(MAPPER::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeVocabularyReference> findByUserId(String userId) {
        return jpaRepo.findAllByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(MAPPER::toDomain)
                .toList();
    }
}
