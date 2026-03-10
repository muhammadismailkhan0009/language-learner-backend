package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlashCardRevisionAlgorithmTests {

    @Test
    @DisplayName("Revision algorithm returns strictly the requested count")
    void returnsStrictlyRequestedCount() {
        var session = new RevisionSession();
        var cards = revisionCards(6);

        var selected = FlashCardAlgorithmService.getCardsForRevision(cards, session, 2);

        assertThat(selected).hasSize(2);
    }

    @Test
    @DisplayName("Revision algorithm does not repeat a card until five other reviews are shown")
    void doesNotRepeatUntilFiveOtherReviewsAreShown() {
        var session = new RevisionSession();
        var cards = revisionCards(6);
        var shownIds = new ArrayList<String>();

        for (int index = 0; index < 6; index++) {
            var selected = FlashCardAlgorithmService.getCardsForRevision(cards, session, 1);
            assertThat(selected).hasSize(1);
            var card = selected.getFirst();
            shownIds.add(card.id().id());
            session.markShown(card.id().id());
        }

        var seventh = FlashCardAlgorithmService.getCardsForRevision(cards, session, 1);

        assertThat(shownIds).doesNotHaveDuplicates();
        assertThat(seventh).hasSize(1);
        assertThat(shownIds).contains(seventh.getFirst().id().id());
    }

    private List<FlashCardReview> revisionCards(int count) {
        var now = Instant.now();
        var cards = new ArrayList<FlashCardReview>();
        for (int index = 1; index <= count; index++) {
            cards.add(new FlashCardReview(
                    new FlashCardReview.FlashCardId("card-" + index),
                    new UserId("user-1"),
                    new ContentId("content-" + index),
                    ContentRefType.VOCABULARY,
                    new Card(
                            5.0,
                            now.plusSeconds(3600),
                            0,
                            0,
                            now.minusSeconds(600),
                            1,
                            1,
                            1,
                            7.0,
                            State.REVIEW
                    ),
                    true
            ));
        }
        return cards;
    }
}
