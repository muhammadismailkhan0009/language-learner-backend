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
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public Optional<FlashCardReview> findReviewInfoByCard(FlashCardReview.FlashCardId id) {
        var entity = flashCardReviewJpaRepo.findById(id.id());
        if (entity.isEmpty()) return Optional.empty();
        var value = entity.get();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(value));
    }

    @Override
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public List<FlashCardReview> findFlashCardsByDeckAndUser(DeckId deckId, String userId) {
        if ("chunks".equalsIgnoreCase(deckId.id())) {
            var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.CHUNK, userId);
            return entities.parallelStream().map(FlashCardMapper.INSTANCE::toModel).toList();
        } else if ("sentences".equalsIgnoreCase(deckId.id())) {
            var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.SENTENCE, userId);
            return entities.parallelStream().map(FlashCardMapper.INSTANCE::toModel).toList();
        } else if ("private_vocabulary".equalsIgnoreCase(deckId.id())) {
            var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId);
            return entities.parallelStream().map(FlashCardMapper.INSTANCE::toModel).toList();
        }
        return List.of();
    }

    @Override
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public void saveFlashCardState(FlashCardReview review) {
        flashCardReviewJpaRepo.save(FlashCardMapper.INSTANCE.toEntity(review));
    }

    @Override
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public void createFlashCard(FlashCardReview flashCardReview) {
        var entity = FlashCardMapper.INSTANCE.toEntity(flashCardReview);
        flashCardReviewJpaRepo.save(entity);
    }

    @Override
    @Deprecated(since = "2026-02-22", forRemoval = true)
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

    @Override
    @Deprecated(since = "2026-02-22", forRemoval = true)
    public Optional<FlashCardReview> getCardAgainstContentAndUserAndDirection(ContentId contentId,
                                                                               ContentRefType contentType,
                                                                               UserId userId,
                                                                               boolean isReversed) {
        var entity = flashCardReviewJpaRepo.findByLanguageContentIdAndContentTypeAndUserIdAndIsReversed(
                contentId.id(),
                contentType,
                userId.id(),
                isReversed
        );
        if (entity.isEmpty()) return Optional.empty();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(entity.get()));
    }

    @Override
    public List<FlashCardReview> findVocabularyFlashCardsByUser(String userId) {
        var entities = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId);
        return entities.parallelStream()
                .map(FlashCardMapper.INSTANCE::toModel)
                .toList();
    }

    @Override
    public Optional<FlashCardReview> findVocabularyReviewInfoByCard(FlashCardReview.FlashCardId id) {
        var entity = flashCardReviewJpaRepo.findById(id.id());
        if (entity.isEmpty()) return Optional.empty();
        var value = entity.get();
        if (!ContentRefType.VOCABULARY.equals(value.getContentType())) return Optional.empty();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(value));
    }

    @Override
    public void saveVocabularyFlashCardState(FlashCardReview review) {
        flashCardReviewJpaRepo.save(FlashCardMapper.INSTANCE.toEntity(review));
    }

    @Override
    public void createVocabularyFlashCard(FlashCardReview review) {
        var entity = FlashCardMapper.INSTANCE.toEntity(review);
        flashCardReviewJpaRepo.save(entity);
    }

    @Override
    public Optional<FlashCardReview> getVocabularyCardAgainstContentAndUserAndDirection(ContentId vocabularyId,
                                                                                         UserId userId,
                                                                                         boolean isReversed) {
        var entity = flashCardReviewJpaRepo.findByLanguageContentIdAndContentTypeAndUserIdAndIsReversed(
                vocabularyId.id(),
                ContentRefType.VOCABULARY,
                userId.id(),
                isReversed
        );
        if (entity.isEmpty()) return Optional.empty();
        return Optional.of(FlashCardMapper.INSTANCE.toModel(entity.get()));
    }
}
