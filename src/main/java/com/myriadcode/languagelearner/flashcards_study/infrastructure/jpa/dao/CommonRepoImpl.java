package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardData;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ids.DeckId;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.mappers.FlashCardMapper;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommonRepoImpl implements FlashCardRepo {

    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    @Override
    public List<FlashCardData> findDataByDeckId(DeckId deckId) {
        if ("chunks".equalsIgnoreCase(deckId.id())) {
//            return chunks data
        } else if ("sentences".equalsIgnoreCase(deckId.id())) {
//            return sentence data
        }
        return List.of();
    }

    @Override
    public Optional<FlashCardReview> findReviewInfoByCard(FlashCardReview.FlashCardId id) {
        var entity = flashCardReviewJpaRepo.findById(id.id());
        if (entity.isEmpty()) return Optional.empty();
        var value = entity.get();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(value));
    }

    @Override
    public List<FlashCardReview> findFlashCardsByDeckAndUser(DeckId deckId, String userId) {
        if ("chunks".equalsIgnoreCase(deckId.id())) {
            var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.CHUNK, userId);
            return entities.parallelStream().map(FlashCardMapper.INSTANCE::toModel).toList();
        } else if ("sentences".equalsIgnoreCase(deckId.id())) {
            var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.SENTENCE, userId);
            return entities.parallelStream().map(FlashCardMapper.INSTANCE::toModel).toList();

        }
        return List.of();
    }

    @Override
    public void saveFlashCardState(FlashCardReview review) {

        var reviewEntity = flashCardReviewJpaRepo.findById(review.id().id());
        if (reviewEntity.isPresent()) {
            var json = review.cardReviewData().toJson();
            var entity = reviewEntity.get();
            entity.setCardJson(json);
            flashCardReviewJpaRepo.save(entity);
        }

    }

    @Override
    public void createFlashCard(FlashCardReview flashCardReview) {
        var entity = FlashCardMapper.INSTANCE.toEntity(flashCardReview);
        flashCardReviewJpaRepo.save(entity);
    }

    @Override
    public Optional<FlashCardReview> getCardAgainstContentAndUser(ContentId contentId,
                                                                  ContentRefType contentType,
                                                                  UserId userId) {
        var entity = flashCardReviewJpaRepo.findByLanguageContentIdAndContentTypeAndUserId(contentId.id(),
                contentType,
                userId.id());
        if (entity.isEmpty()) return Optional.empty();
        var value = entity.get();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(value));
    }
}
