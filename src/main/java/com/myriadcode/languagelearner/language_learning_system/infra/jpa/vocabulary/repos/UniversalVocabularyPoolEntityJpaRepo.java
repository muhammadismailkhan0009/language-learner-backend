package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.UniversalVocabularyPoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversalVocabularyPoolEntityJpaRepo extends JpaRepository<UniversalVocabularyPoolEntity, String> {

    Optional<UniversalVocabularyPoolEntity> findByNormalizedSurfaceAndEntryKind(String normalizedSurface, String entryKind);
}
