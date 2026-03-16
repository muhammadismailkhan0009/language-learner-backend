package com.myriadcode.languagelearner.behavior.flashcards;

import com.myriadcode.fsrs.api.enums.ReviewLogRating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.ReviewLogMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.ReviewLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewLogBoundaryTests {

    @Test
    @DisplayName("Review log json round-trip preserves the mirrored review log structure")
    void jsonRoundTripPreservesMirroredStructure() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var reviewLog = new ReviewLog(
                6.5,
                now.plusSeconds(3600),
                3,
                1,
                2,
                ReviewLogRating.GOOD,
                now,
                5,
                9.2,
                State.REVIEW
        );

        var restored = ReviewLog.fromJson(reviewLog.toJson());

        assertThat(restored).isEqualTo(reviewLog);
    }

    @Test
    @DisplayName("Review log json list round-trip preserves ordered review history")
    void jsonListRoundTripPreservesOrderedReviewHistory() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var reviewLogs = List.of(
                new ReviewLog(
                        6.5,
                        now.plusSeconds(3600),
                        3,
                        1,
                        2,
                        ReviewLogRating.GOOD,
                        now,
                        5,
                        9.2,
                        State.REVIEW
                ),
                new ReviewLog(
                        5.2,
                        now.plusSeconds(7200),
                        5,
                        3,
                        2,
                        ReviewLogRating.EASY,
                        now.plusSeconds(60),
                        8,
                        11.1,
                        State.REVIEW
                )
        );

        var restored = ReviewLog.listFromJson(ReviewLog.listToJson(reviewLogs));

        assertThat(restored).containsExactlyElementsOf(reviewLogs);
    }

    @Test
    @DisplayName("Review log json deserialization supports Instant fields without module auto-discovery")
    void jsonDeserializationSupportsInstantFields() {
        var json = """
                {
                  "difficulty": 6.5,
                  "due": "2026-03-11T13:00:00Z",
                  "elapsedDays": 3,
                  "lastElapsedDays": 1,
                  "learningSteps": 2,
                  "rating": "GOOD",
                  "review": "2026-03-11T12:00:00Z",
                  "scheduledDays": 5,
                  "stability": 9.2,
                  "state": "REVIEW"
                }
                """;

        var restored = ReviewLog.fromJson(json);

        assertThat(restored.due()).isEqualTo(Instant.parse("2026-03-11T13:00:00Z"));
        assertThat(restored.review()).isEqualTo(Instant.parse("2026-03-11T12:00:00Z"));
        assertThat(restored.rating()).isEqualTo(ReviewLogRating.GOOD);
        assertThat(restored.state()).isEqualTo(State.REVIEW);
    }

    @Test
    @DisplayName("Review log library mapping round-trip preserves all mirrored fields")
    void libraryMappingRoundTripPreservesAllFields() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var reviewLog = new ReviewLog(
                4.2,
                now.plusSeconds(1800),
                2,
                1,
                1,
                ReviewLogRating.HARD,
                now.minusSeconds(300),
                2,
                3.7,
                State.LEARNING
        );

        var restored = ReviewLogMapper.toDomain(ReviewLogMapper.toLibrary(reviewLog));

        assertThat(restored).isEqualTo(reviewLog);
    }
}
