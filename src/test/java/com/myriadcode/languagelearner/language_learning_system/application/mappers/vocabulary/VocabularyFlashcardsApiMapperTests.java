package com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyFlashcardsApiMapperTests {

    private static final VocabularyFlashcardsApiMapper MAPPER = VocabularyFlashcardsApiMapper.INSTANCE;

    @Test
    @DisplayName("Maps vocabulary to private vocabulary contract shape including notes")
    void mapsVocabularyToPrivateVocabularyRecord() {
        var vocabulary = new Vocabulary(
                new Vocabulary.VocabularyId("v-1"),
                new UserId("u-1"),
                "lernen",
                "to learn",
                Vocabulary.EntryKind.WORD,
                "verb",
                List.of(
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("s-1"),
                                "Ich lerne Deutsch.",
                                "I am learning German."
                        ),
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("s-2"),
                                "Wir lernen zusammen.",
                                "We study together."
                        )
                ),
                new VocabularyClozeSentence(
                        new VocabularyClozeSentence.VocabularyClozeSentenceId("c-1"),
                        "Ich ___ Deutsch.",
                        "learn",
                        "lerne",
                        List.of("lerne"),
                        "learn"
                ),
                Instant.parse("2026-01-01T00:00:00Z")
        );

        var mapped = MAPPER.toPrivateVocabularyRecord(vocabulary);

        assertThat(mapped.id()).isEqualTo("v-1");
        assertThat(mapped.userId()).isEqualTo("u-1");
        assertThat(mapped.surface()).isEqualTo("lernen");
        assertThat(mapped.translation()).isEqualTo("to learn");
        assertThat(mapped.entryKind()).isEqualTo("WORD");
        assertThat(mapped.notes()).isEqualTo("verb");
        assertThat(mapped.exampleSentences())
                .extracting(PrivateVocabularyRecord.ExampleSentenceRecord::sentence)
                .containsExactly("Ich lerne Deutsch.", "Wir lernen zusammen.");
        assertThat(mapped.clozeSentence()).isNotNull();
        assertThat(mapped.clozeSentence().hint()).isEqualTo("learn");
        assertThat(mapped.clozeSentence().answerWords()).containsExactly("lerne");
    }

    @Test
    @DisplayName("Maps empty example list to empty sentence records list")
    void mapsEmptyExampleSentencesListAsEmpty() {
        var vocabulary = new Vocabulary(
                new Vocabulary.VocabularyId("v-2"),
                new UserId("u-2"),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );

        var mapped = MAPPER.toPrivateVocabularyRecord(vocabulary);

        assertThat(mapped.surface()).isEqualTo("gehen");
        assertThat(mapped.translation()).isEqualTo("to go");
        assertThat(mapped.exampleSentences()).isEmpty();
    }
}
