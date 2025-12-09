package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.ContentGenerationQuantity;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GermanBlitzTest {

    // Shortcut to first lesson of the Blitz syllabus
    private static final GermanBlitz L1 = GermanBlitz.values()[0];
    private static final GermanBlitz L2 = GermanBlitz.values()[1];

    // Helper to create a LangConfigsAdaptive for any lesson
    private LangConfigsAdaptive adapt(GermanBlitz lesson) {
        return new LangConfigsAdaptive(
                lesson.getRule(),
                lesson.getFunction(),
                lesson.getScenario(),
                new LangConfigsAdaptive.GenerationQuantity(
                        ContentGenerationQuantity.SENTENCES.getNumber()
                )
        );
    }

    // ============================================================
    // TEST 1 — No previous lessons → returns L1
    // ============================================================
    @Test
    public void test_NoPreviousLessons_ReturnsFirstLesson() {

        Optional<LangConfigsAdaptive> result =
                GermanBlitz.getNextLessonToGenerateContentFor(List.of());

        assertTrue(result.isPresent());
        var lesson = result.get();

        assertEquals(L1.getScenario(), lesson.scenario());
        assertEquals(L1.getRule(), lesson.rule());
        assertEquals(L1.getFunction(), lesson.function());
    }

    // ============================================================
    // TEST 2 — L1 completed → returns L2
    // ============================================================
    @Test
    public void test_L1Completed_ReturnsL2() {

        List<LangConfigsAdaptive> previous = List.of(adapt(L1));

        var result = GermanBlitz.getNextLessonToGenerateContentFor(previous);

        assertTrue(result.isPresent());
        var lesson = result.get();

        assertEquals(L2.getScenario(), lesson.scenario());
        assertEquals(L2.getRule(), lesson.rule());
        assertEquals(L2.getFunction(), lesson.function());
    }

    // ============================================================
    // TEST 3 — Gap detection: completed L2 but missing L1 → return L1
    // ============================================================
    @Test
    public void test_GapDetection_ReturnsL1() {

        List<LangConfigsAdaptive> previous = List.of(adapt(L2)); // L1 missing

        var result = GermanBlitz.getNextLessonToGenerateContentFor(previous);

        assertTrue(result.isPresent());
        var lesson = result.get();

        assertEquals(L1.getScenario(), lesson.scenario());
        assertEquals(L1.getRule(), lesson.rule());
        assertEquals(L1.getFunction(), lesson.function());
    }

    // ============================================================
    // TEST 4 — Full completion → Optional.empty()
    // ============================================================
    @Test
    public void test_AllCompleted_ReturnsEmpty() {

        List<LangConfigsAdaptive> previous =
                Arrays.stream(GermanBlitz.values())
                        .map(this::adapt)
                        .toList();

        var result = GermanBlitz.getNextLessonToGenerateContentFor(previous);

        assertTrue(result.isEmpty());
    }

    // ============================================================
    // TEST 5 — CEFR values + quantity correct for first lesson
    // ============================================================
    @Test
    public void test_CEFR_AndQuantityCorrect() {

        Optional<LangConfigsAdaptive> result =
                GermanBlitz.getNextLessonToGenerateContentFor(List.of());

        assertTrue(result.isPresent());
        var lesson = result.get();

        assertEquals(L1.getRule().level(), lesson.rule().level());
        assertEquals(L1.getFunction().level(), lesson.function().level());
        assertEquals(L1.getScenario().level(), lesson.scenario().level());

        assertEquals(
                ContentGenerationQuantity.SENTENCES.getNumber(),
                lesson.quantity().sentenceCount()
        );
    }
}
