package com.myriadcode.languagelearner.flashcards_study.domain.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record FsrsCard(
        double difficulty,
        Instant due,
        int elapsedDays,
        int lapses,
        Instant lastReview,
        int learningSteps,
        int reps,
        int scheduledDays,
        double stability,
        State state
) {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize fsrs card", e);
        }
    }

    public static FsrsCard fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, FsrsCard.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to deserialize fsrs card", e);
        }
    }
}
