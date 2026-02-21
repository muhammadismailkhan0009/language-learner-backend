package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExampleSentenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VocabularyJpaMapper {

    VocabularyJpaMapper INSTANCE = Mappers.getMapper(VocabularyJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    VocabularyEntity toEntity(Vocabulary vocabulary);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "vocabulary", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    VocabularyExampleSentenceEntity toExampleSentenceEntity(VocabularyExampleSentence sentence);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    Vocabulary toDomain(VocabularyEntity entity);

    @Mapping(target = "id.id", source = "id")
    VocabularyExampleSentence toExampleSentenceDomain(VocabularyExampleSentenceEntity sentenceEntity);

    default String mapEntryKind(Vocabulary.EntryKind entryKind) {
        return entryKind == null ? null : entryKind.name();
    }

    default Vocabulary.EntryKind mapEntryKind(String entryKind) {
        return entryKind == null ? null : Vocabulary.EntryKind.valueOf(entryKind);
    }
}
