package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.UniversalVocabularyPoolEntry;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.UniversalVocabularyPoolRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers.UniversalVocabularyPoolJpaMapper;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.UniversalVocabularyPoolEntityJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UniversalVocabularyPoolJpaRepoImpl implements UniversalVocabularyPoolRepo {

    private static final UniversalVocabularyPoolJpaMapper MAPPER = UniversalVocabularyPoolJpaMapper.INSTANCE;

    private final UniversalVocabularyPoolEntityJpaRepo jpaRepo;

    public UniversalVocabularyPoolJpaRepoImpl(UniversalVocabularyPoolEntityJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UniversalVocabularyPoolEntry> findByNormalizedSurfaceAndEntryKind(String normalizedSurface,
                                                                                       Vocabulary.EntryKind entryKind) {
        return jpaRepo.findByNormalizedSurfaceAndEntryKind(normalizedSurface, entryKind.name())
                .map(MAPPER::toDomain);
    }

    @Override
    @Transactional
    public UniversalVocabularyPoolEntry save(UniversalVocabularyPoolEntry entry) {
        var saved = jpaRepo.save(MAPPER.toEntity(entry));
        return MAPPER.toDomain(saved);
    }
}
