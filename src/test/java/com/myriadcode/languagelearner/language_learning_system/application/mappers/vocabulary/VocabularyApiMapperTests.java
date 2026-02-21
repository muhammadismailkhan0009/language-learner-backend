package com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyApiMapperTests {

    private static final VocabularyApiMapper MAPPER = VocabularyApiMapper.INSTANCE;

    @Test
    @DisplayName("API mapper create mapping: request sentence maps to domain sentence without id")
    public void toCreateSentenceMapsContentAndKeepsIdNull() {
        var request = new AddVocabularyRequest.ExampleSentenceRequest(
                "Ich lerne Deutsch.",
                "I am learning German."
        );

        var mapped = MAPPER.toCreateSentence(request);

        assertThat(mapped.id()).isNull();
        assertThat(mapped.sentence()).isEqualTo("Ich lerne Deutsch.");
        assertThat(mapped.translation()).isEqualTo("I am learning German.");
    }

    @Test
    @DisplayName("API mapper update mapping: request sentence id maps into nested domain id")
    public void toUpdateSentenceMapsIdAndContent() {
        var request = new UpdateVocabularyRequest.ExampleSentenceUpdateRequest(
                "example-1",
                "Das ist neu.",
                "This is new."
        );

        var mapped = MAPPER.toUpdateSentence(request);

        assertThat(mapped.id()).isNotNull();
        assertThat(mapped.id().id()).isEqualTo("example-1");
        assertThat(mapped.sentence()).isEqualTo("Das ist neu.");
        assertThat(mapped.translation()).isEqualTo("This is new.");
    }

    @Test
    @DisplayName("API mapper response mapping: domain user and sentence ids are preserved")
    public void toResponseMapsDomainSemantics() {
        var domain = new Vocabulary(
                new Vocabulary.VocabularyId("vocab-1"),
                new UserId("user-a"),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                "usage notes",
                List.of(
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("s1"),
                                "Ich gehe nach Hause.",
                                "I go home."
                        )
                )
        );

        var response = MAPPER.toResponse(domain);

        assertThat(response.id()).isEqualTo("vocab-1");
        assertThat(response.userId()).isEqualTo("user-a");
        assertThat(response.entryKind()).isEqualTo(Vocabulary.EntryKind.WORD);
        assertThat(response.exampleSentences()).hasSize(1);
        assertThat(response.exampleSentences().get(0).id()).isEqualTo("s1");
        assertThat(response.exampleSentences().get(0).sentence()).isEqualTo("Ich gehe nach Hause.");
    }

    @Test
    @DisplayName("API mapper edge case: null sentence lists stay null across list mapping")
    public void nullListsRemainNull() {
        assertThat(MAPPER.toCreateSentences(null)).isNull();
        assertThat(MAPPER.toUpdateSentences(null)).isNull();
    }
}
