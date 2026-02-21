package com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VocabularyApiMapper {

    VocabularyApiMapper INSTANCE = Mappers.getMapper(VocabularyApiMapper.class);

    @Mapping(target = "id", ignore = true)
    VocabularyExampleSentence toCreateSentence(AddVocabularyRequest.ExampleSentenceRequest sentence);

    List<VocabularyExampleSentence> toCreateSentences(List<AddVocabularyRequest.ExampleSentenceRequest> sentences);

    @Mapping(target = "id.id", source = "id")
    VocabularyExampleSentence toUpdateSentence(UpdateVocabularyRequest.ExampleSentenceUpdateRequest sentence);

    List<VocabularyExampleSentence> toUpdateSentences(
            List<UpdateVocabularyRequest.ExampleSentenceUpdateRequest> sentences
    );

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    VocabularyResponse toResponse(Vocabulary vocabulary);

    @Mapping(target = "id", source = "id.id")
    VocabularyResponse.ExampleSentenceResponse toSentenceResponse(VocabularyExampleSentence sentence);
}
