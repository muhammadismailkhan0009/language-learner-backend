package com.myriadcode.languagelearner.common.startup;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsRescheduleResultMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
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

    public FlashcardResetRunner(FlashCardReviewJpaRepo flashCardReviewJpaRepo) {
        this.flashCardReviewJpaRepo = flashCardReviewJpaRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        // FIXME: Remove this one-off startup reset runner after the FSRS card reset migration is executed once.
        flashCardReviewJpaRepo.findAll().forEach(this::resetAndReplaceReviewData);
    }

    private void resetAndReplaceReviewData(FlashCardReviewEntity existingEntity) {
        var existingReview = FlashCardMapper.INSTANCE.toModel(existingEntity);
        var resetReview = resetFlashcard(existingReview);
        var replacementEntity = FlashCardMapper.INSTANCE.toEntity(resetReview);

        // One-off migration behavior: replace stored card + logs after reset.
        // Regular study flows must keep append semantics in CardStudyService.
        existingEntity.setCardJson(replacementEntity.getCardJson());
        existingEntity.setReviewLogs(replacementEntity.getReviewLogs());
        flashCardReviewJpaRepo.save(existingEntity);
    }

    private FlashCardReview resetFlashcard(FlashCardReview review) {
        var resetResult = FsrsRescheduleResultMapper.toDomain(
                fsrsEngine.resetScheduling(
                        FsrsCardMapper.toLibrary(review.cardReviewData().card()),
                        Instant.now(),
                        review.isReversed()
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
