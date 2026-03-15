package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.mappers;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;
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
            @Mapping(source = "cardReviewData.card", target = "cardJson", qualifiedByName = "convertCardToJson"),
            @Mapping(source = "cardReviewData.log", target = "reviewLogJson", qualifiedByName = "convertLogToJson")
    })
    FlashCardReviewEntity toEntity(FlashCardReview review);

    @Named("convertCardToJson")
    default String convertCardToJson(FsrsCard review) {
        return review.toJson();
    }

    @Named("convertLogToJson")
    default String convertLogToJson(ReviewLog reviewLog) {
        if (reviewLog == null) {
            return null;
        }
        return reviewLog.toJson();
    }

    @Mappings({
            @Mapping(target = "id.id", source = "id"),
            @Mapping(target = "userId.id", source = "userId"),
            @Mapping(target = "contentId.id", source = "languageContentId"),
            @Mapping(target = "cardReviewData", source = ".", qualifiedByName = "toCardReviewData"),
            @Mapping(target = "isReversed", source = "isReversed")
    })
    FlashCardReview toModel(FlashCardReviewEntity review);

    @Named("toCardReviewData")
    default FsrsRescheduleResult toCardReviewData(FlashCardReviewEntity review) {
        return new FsrsRescheduleResult(
                convertToCard(review.getCardJson()),
                convertToReviewLog(review.getReviewLogJson())
        );
    }

    default FsrsCard convertToCard(String cardJson) {
        return FsrsCard.fromJson(cardJson);
    }

    default ReviewLog convertToReviewLog(String reviewLogJson) {
        if (reviewLogJson == null || reviewLogJson.isBlank()) {
            return null;
        }
        return ReviewLog.fromJson(reviewLogJson);
    }
}
