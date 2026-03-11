package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.ids.ContentId;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
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

    @Test
    @DisplayName("Study algorithm prioritizes overdue lower-stability cards before upcoming cards")
    void studyAlgorithmPrioritizesOverdueAndLowerStability() {
        var now = Instant.now();
        var cards = List.of(
                studyCard("overdue-strong", now.minusSeconds(600), 8.0, 4.0, 0, now.minusSeconds(3600), State.REVIEW),
                studyCard("overdue-weak", now.minusSeconds(600), 2.0, 4.0, 0, now.minusSeconds(7200), State.REVIEW),
                studyCard("upcoming", now.plusSeconds(300), 1.0, 7.0, 1, now.minusSeconds(10800), State.LEARNING)
        );

        var selected = FlashCardAlgorithmService.getCardsForStudy(cards, 3);

        assertThat(selected).extracting(card -> card.id().id())
                .containsExactly("overdue-weak", "overdue-strong", "upcoming");
    }

    @Test
    @DisplayName("Study algorithm falls back to nearest due upcoming cards when nothing is overdue")
    void studyAlgorithmFallsBackToNearestDueUpcomingCards() {
        var now = Instant.now();
        var cards = List.of(
                studyCard("later", now.plusSeconds(3600), 2.0, 5.0, 0, now.minusSeconds(7200), State.REVIEW),
                studyCard("soon", now.plusSeconds(300), 4.0, 4.0, 0, now.minusSeconds(3600), State.REVIEW),
                studyCard("middle", now.plusSeconds(1200), 1.0, 6.0, 1, now.minusSeconds(10800), State.LEARNING)
        );

        var selected = FlashCardAlgorithmService.getCardsForStudy(cards, 3);

        assertThat(selected).extracting(card -> card.id().id())
                .containsExactly("soon", "middle", "later");
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
                    new FsrsCard(
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

    private FlashCardReview studyCard(String id,
                                      Instant due,
                                      double stability,
                                      double difficulty,
                                      int lapses,
                                      Instant lastReview,
                                      State state) {
        return new FlashCardReview(
                new FlashCardReview.FlashCardId(id),
                new UserId("user-1"),
                new ContentId("content-" + id),
                ContentRefType.VOCABULARY,
                new FsrsCard(
                        difficulty,
                        due,
                        0,
                        lapses,
                        lastReview,
                        1,
                        1,
                        1,
                        stability,
                        state
                ),
                true
        );
    }
}
