package com.myriadcode.languagelearner.behavior.reading_paragraph_cloze;

import com.myriadcode.languagelearner.common.enums.ClozePracticeKind;
import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.services.ReadingParagraphClozeGenerationValidator;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ReadingParagraphClozeGenerationValidatorBehaviorTests {
    private final ReadingParagraphClozeGenerationValidator validator = new ReadingParagraphClozeGenerationValidator();

    @Test
    void acceptsVocabularyGrammarAndCombinedExactForms() {
        var generated = generation(
                blank("{{b1}}", "Kindern", "VOCABULARY_FORM", "v1", List.of()),
                blank("{{b2}}", "weil", "GRAMMAR", null, List.of("g1")),
                blank("{{b3}}", "ist gefahren", "VOCABULARY_AND_GRAMMAR", "v2", List.of("g2")));
        assertThat(validator.isValid(generated, Set.of("v1", "v2"), Set.of("g1", "g2"))).isTrue();
    }

    @Test
    void rejectsDuplicateTokensAndUnknownSources() {
        var duplicate = new ClozeParagraphGeneration(List.of(new ClozeParagraphGeneration.Paragraph(
                "Trip", "{{b1}} und {{b1}}.", List.of(blank("{{b1}}", "geht", "VOCABULARY_FORM", "v1", List.of())))));
        var unknown = generation(blank("{{b1}}", "weil", "GRAMMAR", null, List.of("unknown")));
        assertThat(validator.isValid(duplicate, Set.of("v1"), Set.of())).isFalse();
        assertThat(validator.isValid(unknown, Set.of(), Set.of("g1"))).isFalse();
    }

    private ClozeParagraphGeneration generation(ClozeParagraphGeneration.Blank... blanks) {
        var text = String.join(" ", java.util.Arrays.stream(blanks).map(ClozeParagraphGeneration.Blank::blankToken).toList());
        return new ClozeParagraphGeneration(List.of(new ClozeParagraphGeneration.Paragraph("Trip", text, List.of(blanks))));
    }

    private ClozeParagraphGeneration.Blank blank(String token, String answer, String kind, String vocabularyId, List<String> grammarIds) {
        return new ClozeParagraphGeneration.Blank(token, answer, "Explanation", ClozePracticeKind.valueOf(kind), vocabularyId, grammarIds);
    }
}
