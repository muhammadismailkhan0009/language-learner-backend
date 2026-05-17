package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.UniversalVocabularyPoolEntry;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.Optional;

public interface UniversalVocabularyPoolRepo {

    Optional<UniversalVocabularyPoolEntry> findByNormalizedSurfaceAndEntryKind(String normalizedSurface,
                                                                                Vocabulary.EntryKind entryKind);

    UniversalVocabularyPoolEntry save(UniversalVocabularyPoolEntry entry);
}
