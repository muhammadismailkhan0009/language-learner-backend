package com.myriadcode.languagelearner.diagnostic.study;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.configs.TestDbConfigs;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.study.response.StudySessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.services.study.StudyService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos.*;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestDbConfigs.class)
class StudyServiceE2ETest {

    @Autowired
    private StudyService studyService;

    @Autowired
    private VocabularyOrchestrationService vocabularyOrchestrationService;

    @Autowired
    private PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo;

    @Autowired
    private StudySessionJpaRepo studySessionJpaRepo;

    @Autowired
    private StudySessionItemJpaRepo studySessionItemJpaRepo;

    @Autowired
    private StudySentencePoolJpaRepo studySentencePoolJpaRepo;

    @Autowired
    private StudyUserSentenceUsageJpaRepo studyUserSentenceUsageJpaRepo;

    @Autowired
    private StudyAnswerLogJpaRepo studyAnswerLogJpaRepo;

    @Autowired
    private FlashCardReviewJpaRepo flashCardReviewJpaRepo;

    @Autowired
    private VocabularyEntityJpaRepo vocabularyEntityJpaRepo;

    @Autowired
    private Environment environment;

    @AfterEach
    void cleanup() {
        studyAnswerLogJpaRepo.deleteAll();
        studySessionItemJpaRepo.deleteAll();
        studySessionJpaRepo.deleteAll();
        studyUserSentenceUsageJpaRepo.deleteAll();
        studySentencePoolJpaRepo.deleteAll();
        flashCardReviewJpaRepo.deleteAll();
        vocabularyEntityJpaRepo.deleteAll();
    }

    @Test
    @DisplayName("E2E: create study session reactively generates sentence, then evaluates wrong answer via LLM")
    void studyFlowEndToEnd() {
        String apiKeys = environment.getProperty("api-keys", "");
        Assumptions.assumeTrue(apiKeys != null && !apiKeys.trim().isEmpty(),
                "Skipping E2E: property 'api-keys' is empty");

        String userId = "e2e-study-user";

        System.out.println("STEP 1: Seed vocabulary and practice references");
        var created = seedVocabulary(userId);
        created.forEach(v -> practiceVocabularyReferenceRepo.save(new PracticeVocabularyReference(
                new PracticeVocabularyReference.PracticeVocabularyReferenceId(UUID.randomUUID().toString()),
                new UserId(userId),
                v.id(),
                1,
                Instant.now(),
                Instant.now()
        )));

        System.out.println("STEP 2: Create study session (reactive sentence generation path)");
        StudySessionResponse createdSession = studyService.createSession(userId, 1);

        assertThat(createdSession).isNotNull();
        assertThat(createdSession.sessionId()).isNotBlank();
        assertThat(createdSession.status()).isEqualTo("ACTIVE");
        assertThat(createdSession.currentItem()).isNotNull();
        assertThat(createdSession.currentItem().clozeSentence()).contains("____");

        System.out.println(" -> sessionId: " + createdSession.sessionId());
        System.out.println(" -> sentence: " + createdSession.currentItem().clozeSentence());
        System.out.println(" -> expectedAnswer: " + createdSession.currentItem().expectedAnswer());
        System.out.println(" -> hint: " + createdSession.currentItem().hint());

        System.out.println("STEP 3: Submit intentionally wrong answer to trigger LLM evaluation");
        String wrongAnswer = createdSession.currentItem().expectedAnswer() + "x";
        StudySessionResponse afterAnswer = studyService.submitAnswer(
                createdSession.sessionId(),
                createdSession.currentItem().itemId(),
                userId,
                wrongAnswer
        );

        assertThat(afterAnswer).isNotNull();
        assertThat(afterAnswer.appliedRating()).isNotNull();
        assertThat(afterAnswer.feedback()).isNotBlank();

        System.out.println(" -> submittedAnswer: " + wrongAnswer);
        System.out.println(" -> appliedRating: " + afterAnswer.appliedRating());
        System.out.println(" -> feedback: " + afterAnswer.feedback());
        System.out.println(" -> status: " + afterAnswer.status());

        var items = studySessionItemJpaRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc(createdSession.sessionId());
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getRatedAt()).isNotNull();

        var logs = studyAnswerLogJpaRepo.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getMappedRating()).isEqualTo(afterAnswer.appliedRating().name());

        assertThat(studySentencePoolJpaRepo.findAll()).isNotEmpty();
        assertThat(studyUserSentenceUsageJpaRepo.findAllByUserId(userId)).isNotEmpty();
    }

    private List<Vocabulary> seedVocabulary(String userId) {
        var requests = List.of(
                vocab("gehen", "to go"),
                vocab("ankommen", "to arrive"),
                vocab("der Termin", "appointment")
        );

        requests.forEach(request -> vocabularyOrchestrationService.addVocabulary(userId, request));

        return vocabularyEntityJpaRepo.findAll().stream()
                .filter(entity -> userId.equals(entity.getUserId()))
                .map(entity -> new Vocabulary(
                        new Vocabulary.VocabularyId(entity.getId()),
                        new UserId(entity.getUserId()),
                        entity.getSurface(),
                        entity.getTranslation(),
                        Vocabulary.EntryKind.valueOf(entity.getEntryKind()),
                        entity.getNotes(),
                        List.of(),
                        null,
                        entity.getCreatedAt()
                ))
                .toList();
    }

    private AddVocabularyRequest vocab(String surface, String translation) {
        return new AddVocabularyRequest(
                surface,
                translation,
                Vocabulary.EntryKind.WORD,
                null,
                List.of(new AddVocabularyRequest.ExampleSentenceRequest(
                        "Beispiel mit " + surface + ".",
                        "Example with " + surface + "."
                ))
        );
    }
}
