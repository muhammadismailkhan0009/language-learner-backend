package com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.public_vocabulary.entities.PublicVocabularyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PublicVocabularyJpaMapper {

    PublicVocabularyJpaMapper INSTANCE = Mappers.getMapper(PublicVocabularyJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "sourceVocabularyId", source = "sourceVocabularyId.id")
    @Mapping(target = "publishedByUserId", source = "publishedByUserId.id")
    @Mapping(target = "status", source = "status")
    PublicVocabularyEntity toEntity(PublicVocabulary domain);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "sourceVocabularyId.id", source = "sourceVocabularyId")
    @Mapping(target = "publishedByUserId.id", source = "publishedByUserId")
    @Mapping(target = "status", source = "status")
    PublicVocabulary toDomain(PublicVocabularyEntity entity);

    default String mapStatus(PublicVocabulary.PublicVocabularyStatus status) {
        return status == null ? null : status.name();
    }

    default PublicVocabulary.PublicVocabularyStatus mapStatus(String status) {
        return status == null ? null : PublicVocabulary.PublicVocabularyStatus.valueOf(status);
    }

    default Vocabulary.VocabularyId mapSourceVocabularyId(String id) {
        return id == null ? null : new Vocabulary.VocabularyId(id);
    }

    default String mapSourceVocabularyId(Vocabulary.VocabularyId id) {
        return id == null ? null : id.id();
    }

    default UserId mapUserId(String id) {
        return id == null ? null : new UserId(id);
    }

    default String mapUserId(UserId userId) {
        return userId == null ? null : userId.id();
    }
}
