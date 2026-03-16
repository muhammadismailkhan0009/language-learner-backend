package com.myriadcode.languagelearner.common.startup;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsRescheduleResultMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.mappers.FlashCardMapper;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewEntity;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(
        value = "app.fsrs.vocabulary-reset-once.enabled",
        havingValue = "true"
)
public class FlashcardResetRunner implements ApplicationRunner {

    private final FsrsEngine fsrsEngine = FsrsEngine.createDefault();
    private final FlashCardReviewJpaRepo flashCardReviewJpaRepo;
    private final FlashCardRepo flashCardRepo;

    public FlashcardResetRunner(FlashCardReviewJpaRepo flashCardReviewJpaRepo,
                                FlashCardRepo flashCardRepo) {
        this.flashCardReviewJpaRepo = flashCardReviewJpaRepo;
        this.flashCardRepo = flashCardRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        // FIXME: Remove this one-off startup reset runner after the FSRS card reset migration is executed once.
        flashCardReviewJpaRepo.findAll().forEach(this::resetAndReplaceReviewData);
    }

    private void resetAndReplaceReviewData(FlashCardReviewEntity existingEntity) {
        var existingReview = FlashCardMapper.INSTANCE.toModel(existingEntity);
        var resetReview = resetFlashcard(existingReview);
        flashCardRepo.resetFlashCardState(resetReview);
    }

    private FlashCardReview resetFlashcard(FlashCardReview review) {
        var resetResult = FsrsRescheduleResultMapper.toDomain(
                fsrsEngine.resetScheduling(
                        FsrsCardMapper.toLibrary(review.cardReviewData().card()),
                        Instant.now(),
                        true
                )
        );

        return new FlashCardReview(
                review.id(),
                review.userId(),
                review.contentId(),
                review.contentType(),
                resetResult,
                review.isReversed()
        );
    }
}
