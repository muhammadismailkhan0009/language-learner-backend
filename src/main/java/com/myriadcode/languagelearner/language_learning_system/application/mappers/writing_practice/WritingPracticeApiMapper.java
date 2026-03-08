package com.myriadcode.languagelearner.language_learning_system.application.mappers.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingSentencePairResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WritingPracticeApiMapper {

    WritingPracticeApiMapper INSTANCE = Mappers.getMapper(WritingPracticeApiMapper.class);

    @Mapping(target = "sessionId", source = "session.id.id")
    @Mapping(target = "topic", source = "session.topic")
    @Mapping(target = "englishParagraph", source = "session.englishParagraph")
    @Mapping(target = "germanParagraph", source = "session.germanParagraph")
    @Mapping(target = "submittedAnswer", source = "session.submittedAnswer")
    @Mapping(target = "submittedAt", source = "session.submittedAt")
    @Mapping(target = "sentencePairs", source = "session.sentencePairs")
    @Mapping(target = "createdAt", source = "session.createdAt")
    @Mapping(target = "vocabFlashcards", source = "vocabFlashcards")
    WritingPracticeSessionResponse toResponse(WritingPracticeSession session,
                                              java.util.List<WritingVocabularyFlashCardView> vocabFlashcards);

    @Mapping(target = "sessionId", source = "id.id")
    @Mapping(target = "englishParagraphPreview", source = "englishParagraph", qualifiedByName = "preview")
    @Mapping(target = "vocabCount", source = "vocabularyUsages", qualifiedByName = "usageCount")
    @Mapping(target = "submitted", source = "submittedAt", qualifiedByName = "isSubmitted")
    WritingPracticeSessionSummaryResponse toSummary(WritingPracticeSession session);

    WritingSentencePairResponse toSentencePairResponse(
            com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair pair);

    @Named("preview")
    default String preview(String englishParagraph) {
        if (englishParagraph == null) {
            return "";
        }
        var limit = Math.min(englishParagraph.length(), 180);
        return englishParagraph.substring(0, limit);
    }

    @Named("usageCount")
    default int usageCount(java.util.List<?> usages) {
        return usages == null ? 0 : usages.size();
    }

    @Named("isSubmitted")
    default boolean isSubmitted(java.time.Instant submittedAt) {
        return submittedAt != null;
    }
}
