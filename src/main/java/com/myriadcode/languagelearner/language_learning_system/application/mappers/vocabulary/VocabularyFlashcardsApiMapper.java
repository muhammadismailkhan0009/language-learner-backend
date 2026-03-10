package com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VocabularyFlashcardsApiMapper {

    VocabularyFlashcardsApiMapper INSTANCE = Mappers.getMapper(VocabularyFlashcardsApiMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "entryKind", source = "entryKind")
    @Mapping(target = "exampleSentences", source = "exampleSentences")
    @Mapping(target = "clozeSentence", source = "clozeSentence")
    @Mapping(target = "createdAt", source = "createdAt")
    PrivateVocabularyRecord toPrivateVocabularyRecord(Vocabulary vocabulary);

    @Mapping(target = "id", source = "id.id")
    PrivateVocabularyRecord.ExampleSentenceRecord toExampleSentenceRecord(VocabularyExampleSentence sentence);

    @Mapping(target = "id", source = "id.id")
    PrivateVocabularyRecord.ClozeSentenceRecord toClozeSentenceRecord(VocabularyClozeSentence sentence);

    default String map(Vocabulary.EntryKind entryKind) {
        return entryKind == null ? null : entryKind.name();
    }

    default String map(VocabularyExampleSentence.VocabularyExampleSentenceId id) {
        return id == null ? null : id.id();
    }

    default String map(Vocabulary.VocabularyId id) {
        return id == null ? null : id.id();
    }

    default String map(VocabularyClozeSentence.VocabularyClozeSentenceId id) {
        return id == null ? null : id.id();
    }

    default String map(com.myriadcode.languagelearner.common.ids.UserId id) {
        if (id == null) {
            return null;
        }
        return id.id();
    }
}
