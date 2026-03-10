package com.myriadcode.languagelearner.flashcards;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.application.services.CardStudyService;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
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

    @Autowired
    private VocabularyRepo vocabularyRepo;

    @BeforeEach
    void cleanUp() {
        flashCardReviewJpaRepo.deleteAll();
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("WORD vocabulary creates cards on creation")
    void createWordVocabularyGeneratesTwoReferenceCardsAndRendersClozeCardAfterGeneration() {
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
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards).extracting(card -> card.getIsReversed()).containsExactlyInAnyOrder(false, true);

        attachClozeSentence(userId, vocabulary.id(), "Ich ___ Deutsch.", "learn", "lerne", List.of("lerne"), "learn");
        var studyCards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 5);
        assertThat(studyCards).hasSize(1);
        assertThat(studyCards.getFirst().front().clozeText()).isEqualTo("Ich ___ Deutsch.");
        assertThat(studyCards.getFirst().front().hint()).isEqualTo("learn");
        assertThat(studyCards.getFirst().back().answerWords()).containsExactly("lerne");
        assertThat(studyCards.getFirst().back().answerText()).isEqualTo("lerne");
        assertThat(studyCards.getFirst().back().answerTranslation()).isEqualTo("learn");
        assertThat(studyCards.getFirst().back().notes()).isEqualTo("verb");
        assertThat(studyCards.getFirst().isReversed()).isTrue();
    }

    @Test
    @DisplayName("CHUNK vocabulary creates cards on creation")
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
        assertThat(generatedCards).hasSize(2);
        assertThat(generatedCards).extracting(card -> card.getIsReversed()).containsExactlyInAnyOrder(false, true);

        attachClozeSentence(userId, vocabulary.id(), "___ komme ich.", "definitely", "auf jeden Fall", List.of("auf", "jeden", "Fall"), "definitely");
        var studyCards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 5);
        assertThat(studyCards).hasSize(1);
        assertThat(studyCards.getFirst().front().clozeText()).isEqualTo("___ komme ich.");
        assertThat(studyCards.getFirst().back().answerWords()).containsExactly("auf", "jeden", "Fall");
        assertThat(studyCards.getFirst().isReversed()).isTrue();
    }

    @Test
    @DisplayName("Study fetch: a vocabulary card and its reverse wait for five other cards before repeating")
    void studyFetchWaitsForFiveOtherVocabularyEntriesBeforeRepeatingSameClozeCard() {
        var userId = "user-vocab-cards-cooldown-pair";

        for (int index = 1; index <= 6; index++) {
            vocabularyOrchestrationService.addVocabulary(
                    userId,
                    new AddVocabularyRequest(
                            "word-" + index,
                            "translation-" + index,
                            Vocabulary.EntryKind.WORD,
                            null,
                            List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                    "Sentence " + index,
                                    "Translation " + index
                            ))
                    )
            );
        }
        vocabularyEntityJpaRepo.findAll().forEach(entity ->
                attachClozeSentence(userId, entity.getId(), "Sentence ___ " + entity.getId(), "meaning-" + entity.getId(), "fill-" + entity.getId(), List.of("fill-" + entity.getId()), "meaning-" + entity.getId())
        );

        var firstSixVocabularyKeys = new java.util.ArrayList<String>();
        for (int fetchNumber = 0; fetchNumber < 6; fetchNumber++) {
            var fetched = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);
            assertThat(fetched).hasSize(1);
            firstSixVocabularyKeys.add(vocabularyKey(fetched.getFirst()));
        }

        var seventhFetch = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);

        assertThat(firstSixVocabularyKeys).doesNotHaveDuplicates();
        assertThat(seventhFetch).hasSize(1);
        assertThat(vocabularyKey(seventhFetch.getFirst())).isEqualTo(firstSixVocabularyKeys.getFirst());
        assertThat(seventhFetch.getFirst().isReversed()).isTrue();
    }

    @Test
    @DisplayName("Study fetch: when only one vocabulary entry is available, study continues instead of going empty")
    void studyFetchFallsBackToReturnedPoolWhenCooldownWouldEmptyIt() {
        var userId = "user-vocab-cards-single-pool";

        var vocabulary = vocabularyOrchestrationService.addVocabulary(
                userId,
                new AddVocabularyRequest(
                        "lernen",
                        "to learn",
                        Vocabulary.EntryKind.WORD,
                        null,
                        List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                "Ich lerne Deutsch.",
                                "I am learning German."
                        ))
                )
        );

        attachClozeSentence(userId, vocabulary.id(), "Ich ___ Deutsch.", "learn", "lerne", List.of("lerne"), "learn");

        var firstFetch = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);
        var secondFetch = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);

        assertThat(firstFetch).hasSize(1);
        assertThat(secondFetch).hasSize(1);
        assertThat(vocabularyKey(secondFetch.getFirst())).isEqualTo(vocabularyKey(firstFetch.getFirst()));
        assertThat(secondFetch.getFirst().isReversed()).isEqualTo(firstFetch.getFirst().isReversed());
    }

    @Test
    @DisplayName("GOOD review clears the stored cloze sentence for that vocabulary")
    void goodReviewClearsStoredClozeSentence() {
        var userId = "user-vocab-cards-good-review";

        var vocabulary = vocabularyOrchestrationService.addVocabulary(
                userId,
                new AddVocabularyRequest(
                        "vorstellen",
                        "to introduce",
                        Vocabulary.EntryKind.WORD,
                        null,
                        List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                                "Ich stelle meinen Kollegen vor.",
                                "I introduce my colleague."
                        ))
                )
        );

        attachClozeSentence(
                userId,
                vocabulary.id(),
                "Ich ___ meinen Kollegen vor.",
                "introduce",
                "stelle",
                List.of("stelle", "vor"),
                "introduce"
        );

        var studyCards = cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1);

        assertThat(studyCards).hasSize(1);

        cardStudyService.reviewVocabularyStudiedCard(studyCards.getFirst().id(), Rating.GOOD);

        assertThat(vocabularyRepo.findByIdAndUserId(vocabulary.id(), userId).orElseThrow().clozeSentence()).isNull();
        assertThat(cardStudyService.getNextPrivateVocabularyCardsToStudy(userId, 1)).isEmpty();
    }

    private static String vocabularyKey(com.myriadcode.languagelearner.flashcards_study.domain.views.VocabularyFlashCardView card) {
        return card.id();
    }

    private void attachClozeSentence(String userId,
                                     String vocabularyId,
                                     String clozeText,
                                     String hint,
                                     String answerText,
                                     List<String> answerWords,
                                     String answerTranslation) {
        var vocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId).orElseThrow();
        var updated = VocabularyDomainService.withClozeSentence(vocabulary, new VocabularyClozeSentence(
                new VocabularyClozeSentence.VocabularyClozeSentenceId("cloze-" + vocabularyId),
                clozeText,
                hint,
                answerText,
                answerWords,
                answerTranslation
        ));
        vocabularyRepo.replaceClozeSentence(vocabularyId, userId, updated);
    }
}
