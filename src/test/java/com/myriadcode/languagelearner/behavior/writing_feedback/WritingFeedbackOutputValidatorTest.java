package com.myriadcode.languagelearner.behavior.writing_feedback;

import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingFeedbackOutputValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WritingFeedbackOutputValidatorTest {

    private final WritingFeedbackOutputValidator validator = new WritingFeedbackOutputValidator();

    @Test
    void rejectsUnsupportedVocabularyStatus() {
        var result = new WritingVocabularyEvaluationResult(List.of(
                new WritingVocabularyEvaluationResult.Item("vocab-1", "Zug", "almost", "production_good", "Zug", "bad enum")
        ));

        assertThrows(IllegalArgumentException.class, () -> validator.validateVocabulary(result));
    }

    @Test
    void acceptsAllowedVocabularyStatusAndMemorySignal() {
        var result = new WritingVocabularyEvaluationResult(List.of(
                new WritingVocabularyEvaluationResult.Item("vocab-1", "Zug", "partially_correct", "production_hard", "Zug", "wrong article")
        ));

        assertDoesNotThrow(() -> validator.validateVocabulary(result));
    }

    @Test
    void rejectsBlankRequiredComposerSections() {
        var result = new WritingStructuredFeedbackResult(
                "",
                "Korrigierter Absatz.",
                List.of(),
                new WritingStructuredFeedbackResult.VocabularySummary(List.of(), List.of()),
                List.of(),
                List.of(),
                "Perfekt üben"
        );

        assertThrows(IllegalArgumentException.class, () -> validator.validateFeedback(result));
    }

    @Test
    void rejectsMoreThanThreeTopFixes() {
        var fix = new WritingStructuredFeedbackResult.TopFix("Fix", "bad", "good", "short");
        var result = new WritingStructuredFeedbackResult(
                "Meaning: partial",
                "Korrigierter Absatz.",
                List.of(fix, fix, fix, fix),
                new WritingStructuredFeedbackResult.VocabularySummary(List.of(), List.of()),
                List.of(),
                List.of(),
                "Perfekt üben"
        );

        assertThrows(IllegalArgumentException.class, () -> validator.validateFeedback(result));
    }
}
