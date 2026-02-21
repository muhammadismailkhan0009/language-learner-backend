package com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.mappers;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class VocabularyJpaMapperTests {

    private static final VocabularyJpaMapper MAPPER = VocabularyJpaMapper.INSTANCE;

    @Test
    @DisplayName("JPA mapper toEntity: maps user scope, enum, and wires sentence parent relation")
    public void toEntityMapsDomainSemantics() {
        var domain = new Vocabulary(
                new Vocabulary.VocabularyId("v1"),
                new UserId("user-a"),
                "auf jeden Fall",
                "definitely",
                Vocabulary.EntryKind.CHUNK,
                "strong agreement",
                List.of(
                        new VocabularyExampleSentence(
                                new VocabularyExampleSentence.VocabularyExampleSentenceId("e1"),
                                "Auf jeden Fall komme ich.",
                                "I am definitely coming."
                        )
                )
        );

        var entity = MAPPER.toEntity(domain);

        assertThat(entity.getId()).isEqualTo("v1");
        assertThat(entity.getUserId()).isEqualTo("user-a");
        assertThat(entity.getEntryKind()).isEqualTo("CHUNK");
        assertThat(entity.getExampleSentences()).hasSize(1);
        assertThat(entity.getExampleSentences().get(0).getVocabulary()).isSameAs(entity);
        assertThat(entity.getExampleSentences().get(0).getSentence()).isEqualTo("Auf jeden Fall komme ich.");
    }

    @Test
    @DisplayName("JPA mapper toDomain: maps nested ids, enum, and sentence content")
    public void toDomainMapsEntitySemantics() {
        var entity = new VocabularyEntity();
        entity.setId("v2");
        entity.setUserId("user-b");
        entity.setSurface("gehen");
        entity.setTranslation("to go");
        entity.setEntryKind("WORD");
        entity.setNotes("basic verb");

        var sentence = MAPPER.toExampleSentenceEntity(
                new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId("e2"),
                        "Ich gehe nach Hause.",
                        "I go home."
                )
        );
        sentence.setDisplayOrder(0);
        entity.setExampleSentences(List.of(sentence));

        var domain = MAPPER.toDomain(entity);

        assertThat(domain.id().id()).isEqualTo("v2");
        assertThat(domain.userId().id()).isEqualTo("user-b");
        assertThat(domain.entryKind()).isEqualTo(Vocabulary.EntryKind.WORD);
        assertThat(domain.exampleSentences()).hasSize(1);
        assertThat(domain.exampleSentences().get(0).id().id()).isEqualTo("e2");
    }

    @Test
    @DisplayName("JPA mapper edge case: enum helper handles null values")
    public void enumHelpersHandleNull() {
        assertThat(MAPPER.mapEntryKind((String) null)).isNull();
        assertThat(MAPPER.mapEntryKind((Vocabulary.EntryKind) null)).isNull();
    }
}
