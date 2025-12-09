package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.mappers;

import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FlashCardMapper {

    FlashCardMapper INSTANCE = Mappers.getMapper(FlashCardMapper.class);

    @Mappings({
            @Mapping(source = "id.id", target = "id"),
            @Mapping(source = "userId.id", target = "userId"),
            @Mapping(source = "contentId.id", target = "languageContentId"),
            @Mapping(source = "cardReviewData", target = "cardJson", qualifiedByName = "convertToJson")
    })
    FlashCardReviewEntity toEntity(FlashCardReview review);

    @Named("convertToJson")
    default String convertToJson(Card review) {
        return review.toJson();
    }


    @Mappings({
            @Mapping(target = "id.id", source = "id"),
            @Mapping(target = "userId.id", source = "userId"),
            @Mapping(target = "contentId.id", source = "languageContentId"),
            @Mapping(target = "cardReviewData", source = "cardJson", qualifiedByName = "convertToCard"),
            @Mapping(target = "isReversed", source = "isReversed")
    })
    FlashCardReview toModel(FlashCardReviewEntity review);

    @Named("convertToCard")
    default Card convertToCard(String cardJson) {
        return Card.fromJson(cardJson);
    }
}
