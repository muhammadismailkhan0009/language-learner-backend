package com.myriadcode.languagelearner.behavior.vocabulary_extraction;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyDetailSelection;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyDetailValidator;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionValidationException;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VocabularyDetailValidationTests {
    private final VocabularyDetailValidator validator = new VocabularyDetailValidator();

    @Test
    void validatesCompleteDetailsAgainstStoredCandidate() {
        var result = validator.validate(List.of(candidate()), new VocabularyDetailSelection(List.of(
                new VocabularyDetailSelection.Detail("candidate-1", "Bahnhof", "train station", "WORD",
                        "masculine noun", List.of(new VocabularyDetailSelection.ExampleSentence(
                        "Ich warte am Bahnhof.", "I am waiting at the train station."))))));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().candidate().id().id()).isEqualTo("candidate-1");
        assertThat(result.getFirst().translation()).isEqualTo("train station");
    }

    @Test
    void rejectsChangedSurfaceAndInvalidDetailsBeforeCreation() {
        var selection = new VocabularyDetailSelection(List.of(
                new VocabularyDetailSelection.Detail("candidate-1", "Bahnhöfe", "", "INVALID", "", List.of())));

        assertThatThrownBy(() -> validator.validate(List.of(candidate()), selection))
                .isInstanceOf(VocabularyExtractionValidationException.class)
                .hasMessageContaining("surface must not change")
                .hasMessageContaining("Translation is required")
                .hasMessageContaining("Invalid entryKind")
                .hasMessageContaining("example sentence");
    }

    private VocabularyExtractionCandidate candidate() {
        return new VocabularyExtractionCandidate(
                new VocabularyExtractionCandidate.VocabularyExtractionCandidateId("candidate-1"),
                new UserId("user-1"), "Bahnhof", "bahnhof", null, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
