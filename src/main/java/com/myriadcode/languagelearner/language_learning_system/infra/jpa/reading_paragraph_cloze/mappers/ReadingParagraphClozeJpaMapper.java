package com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.mappers;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeCard;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeParagraph;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeCardEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeParagraphEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.entities.ReadingParagraphClozeSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReadingParagraphClozeJpaMapper {

    ReadingParagraphClozeJpaMapper INSTANCE = Mappers.getMapper(ReadingParagraphClozeJpaMapper.class);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "userId.id")
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "paragraphs", ignore = true)
    ReadingParagraphClozeSessionEntity toEntity(ReadingParagraphClozeSession domain);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "userId.id", source = "userId")
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "paragraphs", ignore = true)
    ReadingParagraphClozeSession toDomain(ReadingParagraphClozeSessionEntity entity);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "paragraph", ignore = true)
    ReadingParagraphClozeCardEntity toCardEntity(ReadingParagraphClozeCard card);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "paragraphId", source = "paragraphId")
    ReadingParagraphClozeCard toCardDomain(ReadingParagraphClozeCardEntity entity);

    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "session", ignore = true)
    ReadingParagraphClozeParagraphEntity toParagraphEntity(ReadingParagraphClozeParagraph paragraph);

    @Mapping(target = "id.id", source = "id")
    @Mapping(target = "cards", ignore = true)
    ReadingParagraphClozeParagraph toParagraphDomain(ReadingParagraphClozeParagraphEntity entity);
}
