package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyClozeSentenceEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyExampleSentenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VocabularyJpaMapper {

    VocabularyJpaMapper INSTANCE = Mappers.getMapper(VocabularyJpaMapper.class);
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "createdAt", source = "createdAt")
    VocabularyEntity toEntity(Vocabulary vocabulary);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "vocabulary", ignore = true)
    @Mapping(target = "displayOrder", ignore = true)
    VocabularyExampleSentenceEntity toExampleSentenceEntity(VocabularyExampleSentence sentence);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "vocabulary", ignore = true)
    @Mapping(target = "answerWordsJson", expression = "java(mapAnswerWords(sentence.answerWords()))")
    VocabularyClozeSentenceEntity toClozeSentenceEntity(VocabularyClozeSentence sentence);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "createdAt", source = "createdAt")
    Vocabulary toDomain(VocabularyEntity entity);

    @Mapping(target = "id.id", source = "id")
    VocabularyExampleSentence toExampleSentenceDomain(VocabularyExampleSentenceEntity sentenceEntity);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "answerWords", expression = "java(mapAnswerWords(sentenceEntity.getAnswerWordsJson()))")
    VocabularyClozeSentence toClozeSentenceDomain(VocabularyClozeSentenceEntity sentenceEntity);

    default String mapEntryKind(Vocabulary.EntryKind entryKind) {
        return entryKind == null ? null : entryKind.name();
    }

    default Vocabulary.EntryKind mapEntryKind(String entryKind) {
        return entryKind == null ? null : Vocabulary.EntryKind.valueOf(entryKind);
    }

    default String mapAnswerWords(java.util.List<String> answerWords) {
        try {
            return OBJECT_MAPPER.writeValueAsString(answerWords == null ? java.util.List.of() : answerWords);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to map answerWords to json", e);
        }
    }

    default java.util.List<String> mapAnswerWords(String answerWordsJson) {
        if (answerWordsJson == null || answerWordsJson.isBlank()) {
            return java.util.List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(answerWordsJson, new TypeReference<java.util.List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to map answerWords json", e);
        }
    }
}
