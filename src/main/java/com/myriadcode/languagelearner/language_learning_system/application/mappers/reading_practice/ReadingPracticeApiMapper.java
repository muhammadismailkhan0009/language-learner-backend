package com.myriadcode.languagelearner.language_learning_system.application.mappers.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response.ReadingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.model.ReadingPracticeSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReadingPracticeApiMapper {

    ReadingPracticeApiMapper INSTANCE = Mappers.getMapper(ReadingPracticeApiMapper.class);

    @Mapping(target = "sessionId", source = "id.id")
    ReadingPracticeSessionResponse toResponse(ReadingPracticeSession session);

    @Mapping(target = "sessionId", source = "id.id")
    @Mapping(target = "readingTextPreview", source = "readingText", qualifiedByName = "preview")
    @Mapping(target = "vocabCount", source = "vocabularyUsages", qualifiedByName = "usageCount")
    ReadingPracticeSessionSummaryResponse toSummary(ReadingPracticeSession session);

    @Named("preview")
    default String preview(String readingText) {
        if (readingText == null) {
            return "";
        }
        var limit = Math.min(readingText.length(), 180);
        return readingText.substring(0, limit);
    }

    @Named("usageCount")
    default int usageCount(java.util.List<?> usages) {
        return usages == null ? 0 : usages.size();
    }
}
