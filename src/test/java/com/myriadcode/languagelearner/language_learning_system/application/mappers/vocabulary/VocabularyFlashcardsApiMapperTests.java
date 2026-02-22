package com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyFlashcardsApiMapperTests {

    private static final VocabularyFlashcardsApiMapper MAPPER = VocabularyFlashcardsApiMapper.INSTANCE;

    @Test
    @DisplayName("Maps vocabulary to private vocabulary contract shape excluding notes")
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
                )
        );

        var mapped = MAPPER.toPrivateVocabularyRecord(vocabulary);

        assertThat(mapped.id()).isEqualTo("v-1");
        assertThat(mapped.userId()).isEqualTo("u-1");
        assertThat(mapped.surface()).isEqualTo("lernen");
        assertThat(mapped.translation()).isEqualTo("to learn");
        assertThat(mapped.entryKind()).isEqualTo("WORD");
        assertThat(mapped.exampleSentences())
                .extracting(PrivateVocabularyRecord.ExampleSentenceRecord::sentence)
                .containsExactly("Ich lerne Deutsch.", "Wir lernen zusammen.");
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
                List.of()
        );

        var mapped = MAPPER.toPrivateVocabularyRecord(vocabulary);

        assertThat(mapped.surface()).isEqualTo("gehen");
        assertThat(mapped.translation()).isEqualTo("to go");
        assertThat(mapped.exampleSentences()).isEmpty();
    }
}
