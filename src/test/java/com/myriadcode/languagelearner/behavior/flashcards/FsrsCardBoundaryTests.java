package com.myriadcode.languagelearner.behavior.flashcards;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.flashcards_study.application.mappers.FsrsCardMapper;
import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FsrsCardBoundaryTests {

    @Test
    @DisplayName("Fsrs card json round-trip preserves the mirrored card structure")
    void jsonRoundTripPreservesMirroredStructure() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var card = new FsrsCard(
                6.5,
                now.plusSeconds(3600),
                3,
                1,
                now,
                2,
                7,
                5,
                9.2,
                State.REVIEW
        );

        var restored = FsrsCard.fromJson(card.toJson());

        assertThat(restored).isEqualTo(card);
    }

    @Test
    @DisplayName("Fsrs card json deserialization supports Instant fields without module auto-discovery")
    void jsonDeserializationSupportsInstantFields() {
        var json = """
                {
                  "difficulty": 6.5,
                  "due": "2026-03-11T13:00:00Z",
                  "elapsedDays": 3,
                  "lapses": 1,
                  "lastReview": "2026-03-11T12:00:00Z",
                  "learningSteps": 2,
                  "reps": 7,
                  "scheduledDays": 5,
                  "stability": 9.2,
                  "state": "REVIEW"
                }
                """;

        var restored = FsrsCard.fromJson(json);

        assertThat(restored.due()).isEqualTo(Instant.parse("2026-03-11T13:00:00Z"));
        assertThat(restored.lastReview()).isEqualTo(Instant.parse("2026-03-11T12:00:00Z"));
        assertThat(restored.state()).isEqualTo(State.REVIEW);
    }

    @Test
    @DisplayName("Fsrs card library mapping round-trip preserves all mirrored fields")
    void libraryMappingRoundTripPreservesAllFields() {
        var now = Instant.parse("2026-03-11T12:00:00Z");
        var card = new FsrsCard(
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
        );

        var restored = FsrsCardMapper.toDomain(FsrsCardMapper.toLibrary(card));

        assertThat(restored).isEqualTo(card);
    }
}
