package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.UniversalVocabularyPoolEntry;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.UniversalVocabularyPoolEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UniversalVocabularyPoolJpaMapper {

    UniversalVocabularyPoolJpaMapper INSTANCE = Mappers.getMapper(UniversalVocabularyPoolJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "entryKind", source = "entryKind")
    UniversalVocabularyPoolEntity toEntity(UniversalVocabularyPoolEntry domain);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "entryKind", source = "entryKind")
    UniversalVocabularyPoolEntry toDomain(UniversalVocabularyPoolEntity entity);

    default String mapEntryKind(Vocabulary.EntryKind entryKind) {
        return entryKind == null ? null : entryKind.name();
    }

    default Vocabulary.EntryKind mapEntryKind(String entryKind) {
        return entryKind == null ? null : Vocabulary.EntryKind.valueOf(entryKind);
    }
}
