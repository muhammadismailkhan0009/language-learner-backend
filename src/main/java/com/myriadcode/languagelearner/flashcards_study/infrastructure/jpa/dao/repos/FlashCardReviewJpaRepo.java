package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashCardReviewJpaRepo extends JpaRepository<FlashCardReviewEntity, String> {

    List<FlashCardReviewEntity> findAllByContentTypeAndUserId(ContentRefType contentType, String userId);

    Optional<FlashCardReviewEntity> findByLanguageContentIdAndContentTypeAndUserId(String languageContentId, ContentRefType contentType, String userId);
}
