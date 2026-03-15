package com.myriadcode.languagelearner.flashcards_study.domain.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.myriadcode.fsrs.api.enums.ReviewLogRating;
import com.myriadcode.fsrs.api.enums.State;

import java.time.Instant;

public record ReviewLog(
        double difficulty,
        Instant due,
        int elapsedDays,
        int lastElapsedDays,
        int learningSteps,
        ReviewLogRating rating,
        Instant review,
        int scheduledDays,
        double stability,
        State state
) {

    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize review log", e);
        }
    }

    public static ReviewLog fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, ReviewLog.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to deserialize review log", e);
        }
    }
}
