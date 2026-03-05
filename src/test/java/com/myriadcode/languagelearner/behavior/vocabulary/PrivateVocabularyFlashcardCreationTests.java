package com.myriadcode.languagelearner.behavior.vocabulary;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class PrivateVocabularyFlashcardCreationTests {

    @Autowired
    private VocabularyOrchestrationService vocabularyOrchestrationService;

    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    @Autowired
    private VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    @AfterEach
    void cleanUp() {
        flashCardReviewJpaRepo.deleteAll();
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("addVocabulary: creates private vocabulary flashcards on creation")
    void addVocabularyCreatesFlashcards() {
        var userId = "behavior-user-1";

        vocabularyOrchestrationService.addVocabulary(
                userId,
                new AddVocabularyRequest(
                        "lernen",
                        "to learn",
                        Vocabulary.EntryKind.WORD,
                        "verb",
                        List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                "Ich lerne Deutsch.",
                                "I am learning German."
                        ))
                )
        );

        var generatedCards = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(
                ContentRefType.VOCABULARY,
                userId
        );
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards)
                .extracting(card -> card.getIsReversed())
                .containsExactlyInAnyOrder(false, true);
    }

    @Test
    @DisplayName("updateVocabulary: recreates missing flashcards on update")
    void updateVocabularyRecreatesMissingFlashcards() {
        var userId = "behavior-user-2";

        var created = vocabularyOrchestrationService.addVocabulary(
                userId,
                new AddVocabularyRequest(
                        "gehen",
                        "to go",
                        Vocabulary.EntryKind.WORD,
                        null,
                        List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                "Ich gehe nach Hause.",
                                "I am going home."
                        ))
                )
        );

        flashCardReviewJpaRepo.deleteAll();
        assertThat(flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId))
                .isEmpty();

        var existingExample = created.exampleSentences().getFirst();
        vocabularyOrchestrationService.updateVocabulary(
                userId,
                created.id(),
                new UpdateVocabularyRequest(
                        created.surface(),
                        "to go (updated)",
                        created.entryKind(),
                        created.notes(),
                        List.of(new UpdateVocabularyRequest.ExampleSentenceUpdateRequest(
                                existingExample.id(),
                                existingExample.sentence(),
                                existingExample.translation()
                        ))
                )
        );

        var generatedCards = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(
                ContentRefType.VOCABULARY,
                userId
        );
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards)
                .extracting(card -> card.getIsReversed())
                .containsExactlyInAnyOrder(false, true);
    }

    @Test
    @DisplayName("addVocabulary: rejects duplicate WORD for the same user")
    void addVocabularyRejectsDuplicateWord() {
        var userId = "behavior-user-3";

        var request = new AddVocabularyRequest(
                "gehen",
                "to go",
                Vocabulary.EntryKind.WORD,
                null,
                List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                        "Ich gehe nach Hause.",
                        "I am going home."
                ))
        );

        vocabularyOrchestrationService.addVocabulary(userId, request);

        var differentTranslation = new AddVocabularyRequest(
                "gehen",
                "to walk",
                Vocabulary.EntryKind.WORD,
                null,
                List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                        "Ich gehe nach Hause.",
                        "I am going home."
                ))
        );

        assertThatThrownBy(() -> vocabularyOrchestrationService.addVocabulary(userId, differentTranslation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vocabulary already exists for this user");
    }
}
