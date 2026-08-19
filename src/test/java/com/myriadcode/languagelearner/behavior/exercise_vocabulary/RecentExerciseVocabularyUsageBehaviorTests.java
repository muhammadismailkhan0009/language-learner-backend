package com.myriadcode.languagelearner.behavior.exercise_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecentExerciseVocabularyUsageBehaviorTests {

    @Test
    @DisplayName("Recent usage counts distinct reading and writing sessions in memory")
    void countsDistinctReadingAndWritingSessions() {
        var readingLimit = new AtomicInteger();
        var writingLimit = new AtomicInteger();
        var service = new RecentExerciseVocabularyUsageService(
                (userId, limit) -> {
                    readingLimit.set(limit);
                    return List.of(Set.of("v-1", "v-2"), Set.of("v-1"));
                },
                (userId, limit) -> {
                    writingLimit.set(limit);
                    return List.of(Set.of("v-1", "v-3"), Set.of("v-3"), Set.of());
                }
        );

        var counts = service.countRecentSessionUsage("user-1");

        assertThat(readingLimit).hasValue(10);
        assertThat(writingLimit).hasValue(10);
        assertThat(counts).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "v-1", 3,
                "v-2", 1,
                "v-3", 2
        ));
        assertThatThrownBy(() -> counts.put("v-4", 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Recent usage handles empty and null session histories")
    void handlesEmptyHistories() {
        var service = new RecentExerciseVocabularyUsageService(
                (userId, limit) -> null,
                (userId, limit) -> List.of()
        );

        assertThat(service.countRecentSessionUsage("user-1")).isEmpty();
    }
}
