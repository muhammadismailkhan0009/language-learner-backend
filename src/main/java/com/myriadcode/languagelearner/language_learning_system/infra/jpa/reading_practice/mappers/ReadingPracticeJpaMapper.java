package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_practice.entities.ReadingPracticeVocabularyUsageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReadingPracticeJpaMapper {

    ReadingPracticeJpaMapper INSTANCE = Mappers.getMapper(ReadingPracticeJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    ReadingPracticeSessionEntity toEntity(ReadingPracticeSession session);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "vocabularyUsages", ignore = true)
    ReadingPracticeSession toDomain(ReadingPracticeSessionEntity entity);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "flashcardId", source = "flashCardId")
    @Mapping(target = "session", ignore = true)
    ReadingPracticeVocabularyUsageEntity toUsageEntity(ReadingVocabularyUsage usage);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "flashCardId", source = "flashcardId")
    ReadingVocabularyUsage toUsageDomain(ReadingPracticeVocabularyUsageEntity entity);

    // No metadata mapping (kept minimal for now).
}
