package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VocabularyDomainServiceTests {

    @Test
    @DisplayName("Create vocabulary: assigns identifiers and keeps required fields")
    public void createVocabularyAssignsIds() {
        var vocabulary = VocabularyDomainService.create(
                new UserId("user-1"),
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                "regular verb",
                List.of(
                        new VocabularyExampleSentence(null, "Ich gehe nach Hause.", "I go home."),
                        new VocabularyExampleSentence(null, "Wir gehen jetzt.", "We are going now.")
                )
        );

        assertThat(vocabulary.id().id()).isNotBlank();
        assertThat(vocabulary.userId().id()).isEqualTo("user-1");
        assertThat(vocabulary.surface()).isEqualTo("gehen");
        assertThat(vocabulary.translation()).isEqualTo("to go");
        assertThat(vocabulary.entryKind()).isEqualTo(Vocabulary.EntryKind.WORD);
        assertThat(vocabulary.exampleSentences()).hasSize(2);
        assertThat(vocabulary.exampleSentences())
                .extracting(example -> example.id().id())
                .allMatch(id -> id != null && !id.isBlank());
    }

    @Test
    @DisplayName("Create vocabulary: fails when no example sentence is provided")
    public void createVocabularyFailsWhenExamplesMissing() {
        assertThatThrownBy(() -> VocabularyDomainService.create(
                new UserId("user-1"),
                "bitte",
                "please",
                Vocabulary.EntryKind.WORD,
                null,
                List.of()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one example sentence");
    }

    @Test
    @DisplayName("Edit vocabulary: updates existing examples, adds new, removes omitted")
    public void editVocabularyMergesExampleUpdatesAuthoritatively() {
        var existing = VocabularyDomainService.create(
                new UserId("user-1"),
                "machen",
                "to do",
                Vocabulary.EntryKind.WORD,
                "common verb",
                List.of(
                        new VocabularyExampleSentence(null, "Ich mache Sport.", "I do sports."),
                        new VocabularyExampleSentence(null, "Wir machen Pause.", "We take a break.")
                )
        );

        var toKeepAndUpdate = existing.exampleSentences().get(0);

        var edited = VocabularyDomainService.edit(
                existing,
                "machen",
                "to make/do",
                Vocabulary.EntryKind.WORD,
                "updated note",
                List.of(
                        new VocabularyExampleSentence(
                                toKeepAndUpdate.id(),
                                "Ich mache jetzt Sport.",
                                "I am doing sports now."
                        ),
                        new VocabularyExampleSentence(
                                null,
                                "Sie macht das jeden Tag.",
                                "She does that every day."
                        )
                )
        );

        assertThat(edited.translation()).isEqualTo("to make/do");
        assertThat(edited.notes()).isEqualTo("updated note");
        assertThat(edited.exampleSentences()).hasSize(2);
        assertThat(edited.exampleSentences())
                .extracting(example -> example.sentence() + "|" + example.translation())
                .contains(
                        "Ich mache jetzt Sport.|I am doing sports now.",
                        "Sie macht das jeden Tag.|She does that every day."
                );
    }
}
