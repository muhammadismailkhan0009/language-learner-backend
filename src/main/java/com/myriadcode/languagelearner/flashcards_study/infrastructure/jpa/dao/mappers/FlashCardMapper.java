package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.mappers;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewEntity;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewLogValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface FlashCardMapper {

    FlashCardMapper INSTANCE = Mappers.getMapper(FlashCardMapper.class);

    @Mappings({
            @Mapping(source = "id.id", target = "id"),
            @Mapping(source = "userId.id", target = "userId"),
            @Mapping(source = "contentId.id", target = "languageContentId"),
            @Mapping(source = "cardReviewData.card", target = "cardJson", qualifiedByName = "convertCardToJson"),
            @Mapping(source = "cardReviewData.reviewLogs", target = "reviewLogs", qualifiedByName = "toReviewLogValues")
    })
    FlashCardReviewEntity toEntity(FlashCardReview review);

    @Named("convertCardToJson")
    default String convertCardToJson(FsrsCard review) {
        return review.toJson();
    }

    @Named("toReviewLogValues")
    default List<FlashCardReviewLogValue> toReviewLogValues(List<ReviewLog> reviewLogs) {
        if (reviewLogs == null || reviewLogs.isEmpty()) {
            return List.of();
        }
        return reviewLogs.stream()
                .map(reviewLog -> new FlashCardReviewLogValue(
                        reviewLog.difficulty(),
                        reviewLog.due(),
                        reviewLog.elapsedDays(),
                        reviewLog.lastElapsedDays(),
                        reviewLog.learningSteps(),
                        reviewLog.rating(),
                        reviewLog.review(),
                        reviewLog.scheduledDays(),
                        reviewLog.stability(),
                        reviewLog.state()
                ))
                .toList();
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
                toDomainReviewLogs(review.getReviewLogs())
        );
    }

    default FsrsCard convertToCard(String cardJson) {
        return FsrsCard.fromJson(cardJson);
    }

    default List<ReviewLog> toDomainReviewLogs(List<FlashCardReviewLogValue> reviewLogs) {
        if (reviewLogs == null || reviewLogs.isEmpty()) {
            return List.of();
        }
        return reviewLogs.stream()
                .map(reviewLog -> new ReviewLog(
                        reviewLog.difficulty(),
                        reviewLog.due(),
                        reviewLog.elapsedDays(),
                        reviewLog.lastElapsedDays(),
                        reviewLog.learningSteps(),
                        reviewLog.rating(),
                        reviewLog.review(),
                        reviewLog.scheduledDays(),
                        reviewLog.stability(),
                        reviewLog.state()
                ))
                .toList();
    }
}
