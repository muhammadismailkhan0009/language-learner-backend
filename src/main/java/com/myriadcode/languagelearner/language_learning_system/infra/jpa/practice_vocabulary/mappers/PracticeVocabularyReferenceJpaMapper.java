package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities.PracticeVocabularyReferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PracticeVocabularyReferenceJpaMapper {

    PracticeVocabularyReferenceJpaMapper INSTANCE = Mappers.getMapper(PracticeVocabularyReferenceJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "vocabularyId", source = "vocabularyId.id")
    PracticeVocabularyReferenceEntity toEntity(PracticeVocabularyReference domain);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "vocabularyId.id", source = "vocabularyId")
    PracticeVocabularyReference toDomain(PracticeVocabularyReferenceEntity entity);

    default UserId mapUserId(String userId) {
        return userId == null ? null : new UserId(userId);
    }

    default String mapUserId(UserId userId) {
        return userId == null ? null : userId.id();
    }

    default Vocabulary.VocabularyId mapVocabularyId(String vocabularyId) {
        return vocabularyId == null ? null : new Vocabulary.VocabularyId(vocabularyId);
    }

    default String mapVocabularyId(Vocabulary.VocabularyId vocabularyId) {
        return vocabularyId == null ? null : vocabularyId.id();
    }
}
