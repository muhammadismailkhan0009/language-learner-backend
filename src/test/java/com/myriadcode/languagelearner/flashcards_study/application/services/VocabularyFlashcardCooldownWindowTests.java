package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.fsrs.api.FsrsEngine;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VocabularyFlashcardCooldownWindowTests {

    private final FsrsEngine fsrsEngine = FsrsEngine.createDefault();

    @Test
    @DisplayName("Cooldown window: after five other vocabulary entries pass, the opposite direction is preferred")
    void afterFiveOtherEntriesPassOppositeDirectionIsPreferred() {
        var cooldownWindow = new VocabularyFlashcardCooldownWindow();
        var userId = "user-a";
        var targetFront = flashcard("card-a-front", "vocab-a", false);
        var targetReverse = flashcard("card-a-reverse", "vocab-a", true);

        cooldownWindow.recordShown(userId, List.of(targetFront));
        cooldownWindow.recordShown(userId, List.of(flashcard("card-b-front", "vocab-b", false)));
        cooldownWindow.recordShown(userId, List.of(flashcard("card-c-front", "vocab-c", false)));
        cooldownWindow.recordShown(userId, List.of(flashcard("card-d-front", "vocab-d", false)));
        cooldownWindow.recordShown(userId, List.of(flashcard("card-e-front", "vocab-e", false)));
        cooldownWindow.recordShown(userId, List.of(flashcard("card-f-front", "vocab-f", false)));

        var eligible = cooldownWindow.filterEligible(
                userId,
                List.of(targetFront, targetReverse)
        );

        assertThat(eligible)
                .extracting(card -> card.id().id())
                .containsExactly("card-a-reverse");
    }

    @Test
    @DisplayName("Cooldown window: when every returned card is inside cooldown, study still continues from that returned set")
    void whenAllReturnedCardsAreBlockedStudyStillContinues() {
        var cooldownWindow = new VocabularyFlashcardCooldownWindow();
        var userId = "user-a";
        var targetFront = flashcard("card-a-front", "vocab-a", false);
        var targetReverse = flashcard("card-a-reverse", "vocab-a", true);
        var otherFront = flashcard("card-b-front", "vocab-b", false);

        cooldownWindow.recordShown(userId, List.of(targetFront));
        cooldownWindow.recordShown(userId, List.of(otherFront));

        var eligible = cooldownWindow.filterEligible(
                userId,
                List.of(targetFront, targetReverse, otherFront)
        );

        assertThat(eligible)
                .extracting(card -> card.id().id())
                .contains("card-a-reverse", "card-b-front");
    }

    private FlashCardReview flashcard(String flashcardId, String vocabularyId, boolean isReversed) {
        return new FlashCardReview(
                new FlashCardReview.FlashCardId(flashcardId),
                new UserId("user-a"),
                new ContentId(vocabularyId),
                ContentRefType.VOCABULARY,
                FsrsCardMapper.toDomain(fsrsEngine.createEmptyCard(Instant.now().minusSeconds(60))),
                isReversed
        );
    }
}
