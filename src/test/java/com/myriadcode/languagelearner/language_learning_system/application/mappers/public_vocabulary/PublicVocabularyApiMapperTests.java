package com.myriadcode.languagelearner.language_learning_system.application.mappers.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PublicVocabularyApiMapperTests {

    private static final PublicVocabularyApiMapper MAPPER = PublicVocabularyApiMapper.INSTANCE;

    @Test
    @DisplayName("Public API mapper: merges public metadata and vocabulary content")
    public void toResponseMapsAllFields() {
        var publicVocabulary = new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-1"),
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.parse("2026-01-01T10:15:30Z")
        );

        var vocabulary = new Vocabulary(
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                "basic verb",
                List.of(new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId("ex-1"),
                        "Ich gehe nach Hause.",
                        "I go home."
                ))
        );

        var response = MAPPER.toResponse(publicVocabulary, vocabulary);

        assertThat(response.publicVocabularyId()).isEqualTo("pub-1");
        assertThat(response.sourceVocabularyId()).isEqualTo("vocab-1");
        assertThat(response.publishedByUserId()).isEqualTo("user-a");
        assertThat(response.entryKind()).isEqualTo(Vocabulary.EntryKind.WORD);
        assertThat(response.exampleSentences()).hasSize(1);
        assertThat(response.exampleSentences().get(0).id()).isEqualTo("ex-1");
    }

    @Test
    @DisplayName("Public API mapper edge case: null inputs return null")
    public void toResponseReturnsNullWhenAnyInputNull() {
        var vocabulary = new Vocabulary(
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                null,
                List.of()
        );

        assertThat(MAPPER.toResponse(null, vocabulary)).isNull();
        assertThat(MAPPER.toResponse(new PublicVocabulary(
                new PublicVocabulary.PublicVocabularyId("pub-1"),
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                PublicVocabulary.PublicVocabularyStatus.PUBLISHED,
                Instant.now()
        ), null)).isNull();
    }
}
