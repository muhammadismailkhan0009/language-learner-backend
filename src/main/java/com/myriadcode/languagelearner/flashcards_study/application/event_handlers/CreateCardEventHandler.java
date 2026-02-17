package com.myriadcode.languagelearner.flashcards_study.application.event_handlers;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.repos.FlashCardRepo;
import com.myriadcode.languagelearner.language_content.domain.events.CreateCardEvent;
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
    public void handle(CreateCardEvent event) {
        System.out.println("Storage received content: " + event.getContentId());
        var existingEntity = flashCardRepo.getCardAgainstContentAndUser(
                new ContentId(event.getContentId()), event.getContentType(),
                new UserId(event.getUserId()));
        if (existingEntity.isEmpty()) {
            var flashcard = new FlashCardReview(new FlashCardReview.FlashCardId(UUID.randomUUID().toString()),
                    new UserId(event.getUserId()),
                    new ContentId(event.getContentId()),
                    event.getContentType(),
                    fsrsEngine.createEmptyCard(Instant.now()),
                    false);
           flashCardRepo.createFlashCard(flashcard);

            if(event.isReversed()){
                var reversedCard = new FlashCardReview(new FlashCardReview.FlashCardId(UUID.randomUUID().toString()),
                        new UserId(event.getUserId()),
                        new ContentId(event.getContentId()),
                        event.getContentType(),
                        fsrsEngine.createEmptyCard(Instant.now()),
                        true);
               flashCardRepo.createFlashCard(reversedCard);
            }
        }
    }
}
