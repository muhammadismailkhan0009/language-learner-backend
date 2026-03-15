package com.myriadcode.languagelearner.flashcards_study.application.mappers;

import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;

public final class ReviewLogMapper {

    private ReviewLogMapper() {
    }

    public static ReviewLog toDomain(com.myriadcode.fsrs.api.models.ReviewLog reviewLog) {
        if (reviewLog == null) {
            return null;
        }
        return new ReviewLog(
                reviewLog.difficulty(),
                reviewLog.due(),
                reviewLog.elapsedDays(),
                reviewLog.lastElapsedDays(),
                reviewLog.learningSteps(),
                reviewLog.rating(),
                reviewLog.review(),
                reviewLog.scheduledDays(),
                reviewLog.stability(),
                reviewLog.state()
        );
    }

    public static com.myriadcode.fsrs.api.models.ReviewLog toLibrary(ReviewLog reviewLog) {
        if (reviewLog == null) {
            return null;
        }
        return new com.myriadcode.fsrs.api.models.ReviewLog(
                reviewLog.difficulty(),
                reviewLog.due(),
                reviewLog.elapsedDays(),
                reviewLog.lastElapsedDays(),
                reviewLog.learningSteps(),
                reviewLog.rating(),
                reviewLog.review(),
                reviewLog.scheduledDays(),
                reviewLog.stability(),
                reviewLog.state()
        );
    }
}
