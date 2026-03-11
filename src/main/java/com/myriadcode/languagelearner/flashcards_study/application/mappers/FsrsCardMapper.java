package com.myriadcode.languagelearner.flashcards_study.application.mappers;

import com.myriadcode.fsrs.api.models.Card;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;

public final class FsrsCardMapper {

    private FsrsCardMapper() {
    }

    public static FsrsCard toDomain(Card card) {
        if (card == null) {
            return null;
        }
        return new FsrsCard(
                card.difficulty(),
                card.due(),
                card.elapsedDays(),
                card.lapses(),
                card.lastReview(),
                card.learningSteps(),
                card.reps(),
                card.scheduledDays(),
                card.stability(),
                card.state()
        );
    }

    public static Card toLibrary(FsrsCard card) {
        if (card == null) {
            return null;
        }
        return new Card(
                card.difficulty(),
                card.due(),
                card.elapsedDays(),
                card.lapses(),
                card.lastReview(),
                card.learningSteps(),
                card.reps(),
                card.scheduledDays(),
                card.stability(),
                card.state()
        );
    }
}
