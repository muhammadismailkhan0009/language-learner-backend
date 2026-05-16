package com.myriadcode.languagelearner.diagnostic.reading_paragraph_cloze;

import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze.ReadingParagraphClozeService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.reading_paragraph_cloze.repos.ReadingParagraphClozeSessionJpaRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class ReadingParagraphClozeServiceE2ETest {

    @Autowired
    private ReadingParagraphClozeService readingParagraphClozeService;

    @Autowired
    private VocabularyOrchestrationService vocabularyOrchestrationService;

    @Autowired
    private ReadingParagraphClozeSessionJpaRepo readingParagraphClozeSessionJpaRepo;

    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    @Autowired
    private VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    @AfterEach
    void cleanup() {
        readingParagraphClozeSessionJpaRepo.deleteAll();
        flashCardReviewJpaRepo.deleteAll();
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("E2E: real LLM call generates and persists multiple reading cloze paragraphs")
    void createSessionGeneratesMultipleParagraphsEndToEnd() {
        String userId = "e2e-reading-cloze-user";

        seedVocabulary(userId);

        ReadingParagraphClozeSessionResponse response = readingParagraphClozeService.createSession(userId, 24);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isNotBlank();
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.cards()).isNotEmpty();
        assertThat(response.clozeParagraph()).contains("___");

        var persisted = readingParagraphClozeSessionJpaRepo.findByIdAndUserId(response.sessionId(), userId).orElseThrow();
        assertThat(persisted.getParagraphs())
                .withFailMessage("Expected more than 1 generated paragraph in persisted reading-cloze session")
                .hasSizeGreaterThan(1);
        assertThat(persisted.getCards()).isNotEmpty();
        assertThat(persisted.getCards())
                .allSatisfy(card -> assertThat(card.getParagraphId()).isNotBlank());

        System.out.println("=== Reading Cloze E2E Summary ===");
        System.out.println("Session ID: " + response.sessionId());
        System.out.println("Paragraph count: " + persisted.getParagraphs().size());
        System.out.println("Card count: " + persisted.getCards().size());
        persisted.getParagraphs().forEach(paragraph -> {
            System.out.println("- [" + paragraph.getParagraphIndex() + "] " + paragraph.getScenarioLabel());
            System.out.println("  " + paragraph.getClozeParagraph());
        });
    }

    private void seedVocabulary(String userId) {
        var entries = List.of(
                vocab("abfahren", "to depart"),
                vocab("ankommen", "to arrive"),
                vocab("umsteigen", "to transfer"),
                vocab("verspäten", "to be delayed"),
                vocab("verschieben", "to postpone"),
                vocab("pünktlich", "on time"),
                vocab("die Verbindung", "connection"),
                vocab("der Termin", "appointment"),
                vocab("die Besprechung", "meeting"),
                vocab("besprechen", "to discuss"),
                vocab("vereinbaren", "to arrange"),
                vocab("absagen", "to cancel"),
                vocab("die Abfahrt", "departure"),
                vocab("die Ankunft", "arrival"),
                vocab("warten", "to wait"),
                vocab("erreichen", "to reach")
        );

        entries.forEach(entry -> vocabularyOrchestrationService.addVocabulary(userId, entry));
    }

    private AddVocabularyRequest vocab(String surface, String translation) {
        return new AddVocabularyRequest(
                surface,
                translation,
                com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary.EntryKind.WORD,
                null,
                List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                        "Beispiel mit " + surface + ".",
                        "Example with " + surface + "."
                ))
        );
    }
}

