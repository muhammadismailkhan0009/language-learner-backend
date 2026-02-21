package com.myriadcode.languagelearner.language_learning_system.application.mappers.public_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.response.PublicVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PublicVocabularyApiMapper {

    PublicVocabularyApiMapper INSTANCE = Mappers.getMapper(PublicVocabularyApiMapper.class);

    @Mapping(target = "id", source = "id.id")
    PublicVocabularyResponse.ExampleSentenceResponse toExampleSentenceResponse(VocabularyExampleSentence sentence);

    default PublicVocabularyResponse toResponse(PublicVocabulary publicVocabulary, Vocabulary vocabulary) {
        if (publicVocabulary == null || vocabulary == null) {
            return null;
        }

        return new PublicVocabularyResponse(
                publicVocabulary.id().id(),
                publicVocabulary.sourceVocabularyId().id(),
                publicVocabulary.publishedByUserId().id(),
                publicVocabulary.publishedAt(),
                vocabulary.entryKind(),
                vocabulary.surface(),
                vocabulary.translation(),
                vocabulary.notes(),
                vocabulary.exampleSentences() == null
                        ? java.util.List.of()
                        : vocabulary.exampleSentences().stream().map(this::toExampleSentenceResponse).toList()
        );
    }
}
