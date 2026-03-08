package com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSentencePairEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeSessionEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.writing_practice.entities.WritingPracticeVocabularyUsageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WritingPracticeJpaMapper {

    WritingPracticeJpaMapper INSTANCE = Mappers.getMapper(WritingPracticeJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "sentencePairs", ignore = true)
    @Mapping(target = "vocabularyUsages", ignore = true)
    WritingPracticeSessionEntity toEntity(WritingPracticeSession session);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "sentencePairs", ignore = true)
    @Mapping(target = "vocabularyUsages", ignore = true)
    WritingPracticeSession toDomain(WritingPracticeSessionEntity entity);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "position", source = "position")
    WritingPracticeSentencePairEntity toSentencePairEntity(WritingSentencePair pair);

    @Mapping(target = "id.id", source = "id")
    WritingSentencePair toSentencePairDomain(WritingPracticeSentencePairEntity entity);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "flashcardId", source = "flashCardId")
    @Mapping(target = "session", ignore = true)
    WritingPracticeVocabularyUsageEntity toUsageEntity(WritingVocabularyUsage usage);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "flashCardId", source = "flashcardId")
    WritingVocabularyUsage toUsageDomain(WritingPracticeVocabularyUsageEntity entity);
}
