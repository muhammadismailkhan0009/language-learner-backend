package com.myriadcode.languagelearner.behavior.flashcards;

import com.myriadcode.fsrs.api.enums.ReviewLogRating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsRescheduleResultMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FsrsRescheduleResultBoundaryTests {

    @Test
    @DisplayName("Fsrs reschedule result library mapping round-trip preserves mirrored card and log")
    void libraryMappingRoundTripPreservesMirroredCardAndLog() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var result = new FsrsRescheduleResult(
                new FsrsCard(
                        4.2,
                        now.plusSeconds(1800),
                        2,
                        0,
                        now.minusSeconds(300),
                        1,
                        4,
                        2,
                        3.7,
                        State.LEARNING
                ),
                List.of(
                        new ReviewLog(
                                4.2,
                                now.plusSeconds(1800),
                                2,
                                1,
                                1,
                                ReviewLogRating.HARD,
                                now,
                                2,
                                3.7,
                                State.LEARNING
                        )
                )
        );

        var restored = FsrsRescheduleResultMapper.toDomain(FsrsRescheduleResultMapper.toLibrary(result));

        assertThat(restored).isEqualTo(result);
    }
}
