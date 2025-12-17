package com.myriadcode.languagelearner.language_content.domain.services;

import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanBlitz;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyllabusPolicyTest {

    SyllabusPolicy policy = new SyllabusPolicy();

    LangConfigsAdaptive languageConfigs = new LangConfigsAdaptive(

            GermanAdaptive.GrammarRuleEnum.BASIC_PREPOSITIONS,
            GermanAdaptive.CommunicativeFunctionEnum.ASK_AND_ANSWER_SIMPLE_QUESTIONS,
            GermanAdaptive.ScenarioEnum.DIRECTIONS_AND_LOCATIONS,
            new LangConfigsAdaptive.GenerationQuantity(8)
    );

    @Test
    void assigns_first_syllabus_when_no_history_exists() {
        var now = LocalDateTime.of(2025, 1, 1, 10, 0);

        Optional<LangConfigsAdaptive> result =
                policy.decideNext(List.of(), now);

        assertTrue(result.isPresent());
        assertEquals(
                GermanBlitz.getNextLessonToGenerateContentFor(List.of()).get(),
                result.get()
        );
    }

    @Test
    void does_not_assign_when_content_was_already_assigned_today() {
        var now = LocalDateTime.of(2025, 1, 2, 10, 0);

        var stats = List.of(
                new UserStatsForContent(
                        languageConfigs,
                        LocalDateTime.of(2025, 1, 2, 1, 30), // same calendar day
                        null
                )
        );

        Optional<LangConfigsAdaptive> result =
                policy.decideNext(stats, now);

        assertTrue(result.isEmpty());
    }


    @Test
    void assigns_next_syllabus_after_24_hours() {

        var now = LocalDateTime.of(2025, 1, 2, 10, 0);


        var stats = List.of(
                new UserStatsForContent(
                        languageConfigs,
                        now.minusHours(25),
                        null
                )
        );

        Optional<LangConfigsAdaptive> result =
                policy.decideNext(stats, now);

        assertTrue(result.isPresent());
    }


}