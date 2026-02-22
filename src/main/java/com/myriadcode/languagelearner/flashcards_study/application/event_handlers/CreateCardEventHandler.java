package com.myriadcode.languagelearner.flashcards_study.application.event_handlers;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.CreateFlashCardEvent;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateCardEventHandler {

    private final FlashCardRepo flashCardRepo;

    private final FsrsEngine fsrsEngine;

    public CreateCardEventHandler(FlashCardRepo flashCardRepo) {
        this.flashCardRepo = flashCardRepo;
        this.fsrsEngine = FsrsEngine.createDefault();
    }

    @EventListener
    public void handle(CreateFlashCardEvent event) {
        var contentId = new ContentId(event.getContentId());
        var userId = new UserId(event.getUserId());

        if (ContentRefType.VOCABULARY.equals(event.getContentType())) {
            if (vocabularyCardExists(contentId, userId)) {
                return;
            }
            ensureVocabularyCardExists(contentId, userId, false);
            if (event.isReversed()) {
                ensureVocabularyCardExists(contentId, userId, true);
            }
            return;
        }

        ensureLegacyCardExists(contentId, event, userId, false);
        if (event.isReversed()) {
            ensureLegacyCardExists(contentId, event, userId, true);
        }
    }

    private boolean vocabularyCardExists(ContentId vocabularyId, UserId userId) {
        return flashCardRepo.getVocabularyCardAgainstContentAndUserAndDirection(
                vocabularyId,
                userId,
                false
        ).isPresent() || flashCardRepo.getVocabularyCardAgainstContentAndUserAndDirection(
                vocabularyId,
                userId,
                true
        ).isPresent();
    }

    private void ensureLegacyCardExists(ContentId contentId,
                                        CreateFlashCardEvent event,
                                        UserId userId,
                                        boolean isReversed) {
        var existingEntity = flashCardRepo.getCardAgainstContentAndUserAndDirection(
                contentId,
                event.getContentType(),
                userId,
                isReversed
        );
        if (existingEntity.isPresent()) {
            return;
        }
        var flashcard = new FlashCardReview(
                new FlashCardReview.FlashCardId(UUID.randomUUID().toString()),
                userId,
                contentId,
                event.getContentType(),
                fsrsEngine.createEmptyCard(Instant.now()),
                isReversed
        );
        flashCardRepo.createFlashCard(flashcard);
    }

    private void ensureVocabularyCardExists(ContentId vocabularyId,
                                            UserId userId,
                                            boolean isReversed) {
        var existingEntity = flashCardRepo.getVocabularyCardAgainstContentAndUserAndDirection(
                vocabularyId,
                userId,
                isReversed
        );
        if (existingEntity.isPresent()) {
            return;
        }
        var flashcard = new FlashCardReview(
                new FlashCardReview.FlashCardId(UUID.randomUUID().toString()),
                userId,
                vocabularyId,
                ContentRefType.VOCABULARY,
                fsrsEngine.createEmptyCard(Instant.now()),
                isReversed
        );
        flashCardRepo.createVocabularyFlashCard(flashcard);
    }
}
