package com.myriadcode.languagelearner.flashcards;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
@DisplayName("Private Vocabulary Flashcard Flow")
public class PrivateVocabularyFlashCardFlowTest {

    @Autowired
    private VocabularyOrchestrationService vocabularyOrchestrationService;

    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    @Autowired
    private VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    @Autowired
    private CardStudyService cardStudyService;

    @BeforeEach
    void cleanUp() {
        flashCardReviewJpaRepo.deleteAll();
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("WORD vocabulary creates cards only after explicit generation action")
    void createWordVocabularyGeneratesTwoReferenceCardsAndRendersGermanExamples() {
        var userId = "user-vocab-cards";

        var vocabulary = vocabularyOrchestrationService.addVocabulary(
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
        assertThat(generatedCards).isEmpty();

        vocabularyOrchestrationService.createFlashCardsForVocabulary(userId, vocabulary.id());

        generatedCards = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId);
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards).extracting(card -> card.getIsReversed()).containsExactlyInAnyOrder(false, true);

        var studyCards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 5);
        assertThat(studyCards).hasSize(2);
        assertThat(studyCards)
                .extracting(
                        card -> card.front().wordOrChunk(),
                        card -> card.back().wordOrChunk(),
                        card -> card.back().sentences().stream().map(sentence -> sentence.sentence()).toList(),
                        card -> card.isReversed()
                )
                .containsExactlyInAnyOrder(
                        tuple("lernen", "to learn", List.of("Ich lerne Deutsch."), false),
                        tuple("to learn", "lernen", List.of("Ich lerne Deutsch."), true)
                );
    }

    @Test
    @DisplayName("CHUNK vocabulary also creates cards only after explicit generation action")
    void chunkVocabularyCreatesTwoPrivateVocabularyCards() {
        var userId = "user-vocab-cards-chunk";

        var vocabulary = vocabularyOrchestrationService.addVocabulary(
                userId,
                new AddVocabularyRequest(
                        "auf jeden Fall",
                        "definitely",
                        Vocabulary.EntryKind.CHUNK,
                        null,
                        List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                "Auf jeden Fall komme ich.",
                                "I am definitely coming."
                        ))
                )
        );

        var generatedCards = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(
                ContentRefType.VOCABULARY,
                userId
        );
        assertThat(generatedCards).isEmpty();

        vocabularyOrchestrationService.createFlashCardsForVocabulary(userId, vocabulary.id());

        generatedCards = flashCardReviewJpaRepo.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId);
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards).extracting(card -> card.getIsReversed()).containsExactlyInAnyOrder(false, true);

        var studyCards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 5);
        assertThat(studyCards).hasSize(2);
        assertThat(studyCards)
                .extracting(
                        card -> card.front().wordOrChunk(),
                        card -> card.back().wordOrChunk(),
                        card -> card.back().sentences().stream().map(sentence -> sentence.sentence()).toList(),
                        card -> card.isReversed()
                )
                .containsExactlyInAnyOrder(
                        tuple("auf jeden Fall", "definitely", List.of("Auf jeden Fall komme ich."), false),
                        tuple("definitely", "auf jeden Fall", List.of("Auf jeden Fall komme ich."), true)
                );
    }
}
