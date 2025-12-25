package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german;

public enum GermanExerciseType {

    // ============================================================
    // BEGINNER EXERCISES (A1–A2)
    // ============================================================

    // Listening comprehension (audio → meaning)
    LISTEN_AND_COMPREHEND_SENTENCE(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),

    // Vocabulary & form
    GAP_FILL(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),
    CLOZE_WORD(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),
    CLOZE_CHUNK(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),
    CLOZE_ARTICLE(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),
    PICK_CORRECT_WORD(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),

    // Syntax / structure
    REORDER_SENTENCE(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),
    YES_NO_GRAMMAR_CHECK(ExerciseDifficultyGroup.BEGINNER, GermanAdaptive.CEFR.A1),

    // ============================================================
    // INTERMEDIATE EXERCISES (B1)
    // ============================================================

    CLOZE_GRAMMAR(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    TRANSLATION_L1_TO_L2(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    TRANSLATION_L2_TO_L1(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),

    TRANSFORM_NEGATIVE(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    TRANSFORM_QUESTION(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    TRANSFORM_TENSE(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),

    CHUNK_ASSEMBLY(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    MULTI_SENTENCE_GAP_FILL(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),
    SHORT_DIALOG_COMPLETION(ExerciseDifficultyGroup.INTERMEDIATE, GermanAdaptive.CEFR.B1),

    // ============================================================
    // ADVANCED EXERCISES (B2–C2)
    // ============================================================

    PARAGRAPH_TRANSLATION_L1_TO_L2(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.B2),
    PARAGRAPH_TRANSLATION_L2_TO_L1(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.B2),

    SUMMARIZATION(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.B2),

    SCENARIO_REWRITING(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C1),
    ROLEPLAY_RESPONSE(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C1),
    COMPLEX_SENTENCE_REORDER(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C1),//FIXME: remove this
    MULTI_CLAUSE_TRANSFORMATION(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C1),
    ARGUMENT_RESTRUCTURING(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C1),

    REWRITE_WITH_GRAMMAR_CONSTRAINTS(ExerciseDifficultyGroup.ADVANCED, GermanAdaptive.CEFR.C2);

    // =========================
    // FIELDS + CONSTRUCTOR
    // =========================

    private final ExerciseDifficultyGroup difficultyGroup;
    private final GermanAdaptive.CEFR level;

    GermanExerciseType(ExerciseDifficultyGroup difficultyGroup, GermanAdaptive.CEFR level) {
        this.difficultyGroup = difficultyGroup;
        this.level = level;
    }

    public ExerciseDifficultyGroup getDifficultyGroup() {
        return difficultyGroup;
    }

    public GermanAdaptive.CEFR getLevel() {
        return level;
    }

    // ============================================================
    // EXERCISE DIFFICULTY GROUPS
    // ============================================================

    public enum ExerciseDifficultyGroup {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
}
