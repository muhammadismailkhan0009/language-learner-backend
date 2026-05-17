package com.myriadcode.languagelearner.behavior.study;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.study.StudyService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.entities.*;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.study.repos.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StudyServiceBehaviorTests {

    private final StudySessionJpaRepo sessionRepo = mock(StudySessionJpaRepo.class);
    private final StudySessionItemJpaRepo itemRepo = mock(StudySessionItemJpaRepo.class);
    private final StudySentencePoolJpaRepo sentencePoolRepo = mock(StudySentencePoolJpaRepo.class);
    private final StudyUserSentenceUsageJpaRepo usageRepo = mock(StudyUserSentenceUsageJpaRepo.class);
    private final StudyAnswerLogJpaRepo answerLogRepo = mock(StudyAnswerLogJpaRepo.class);
    private final PracticeVocabularyReferenceRepo practiceRepo = mock(PracticeVocabularyReferenceRepo.class);
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi = mock(FetchVocabularyFlashcardReviewsApi.class);
    private final VocabularyRepo vocabularyRepo = mock(VocabularyRepo.class);
    private final VocabularyClozeLlmApi vocabularyClozeLlmApi = mock(VocabularyClozeLlmApi.class);
    private final StudyAnswerEvaluationLlmApi studyAnswerEvaluationLlmApi = mock(StudyAnswerEvaluationLlmApi.class);
    private final ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi = mock(ReviewVocabularyFlashcardApi.class);

    private final StudyService service = new StudyService(
            sessionRepo,
            itemRepo,
            sentencePoolRepo,
            usageRepo,
            answerLogRepo,
            practiceRepo,
            flashcardReviewsApi,
            vocabularyRepo,
            vocabularyClozeLlmApi,
            studyAnswerEvaluationLlmApi,
            reviewVocabularyFlashcardApi
    );

    @Test
    @DisplayName("createSession: reactive sentence generation path creates session item and returns active item")
    void createSessionReactiveGenerationPath() {
        System.out.println("STEP 1: Arrange mocks for reactive sentence generation");

        when(sessionRepo.findFirstByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(Optional.empty());

        var ref = new PracticeVocabularyReference(
                new PracticeVocabularyReference.PracticeVocabularyReferenceId("ref-1"),
                new UserId("user-1"),
                new Vocabulary.VocabularyId("v-1"),
                1,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        when(practiceRepo.findByUserId("user-1")).thenReturn(List.of(ref));

        when(vocabularyRepo.findByIds(List.of("v-1"))).thenReturn(List.of(vocab("v-1", "gehen", "to go")));

        when(flashcardReviewsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("f-1", "v-1", State.REVIEW, Instant.now().minusSeconds(300), 0.42, 2.0, 3.0, 1, Instant.now().minusSeconds(7200), true)
        ));

        when(usageRepo.findAllByUserId("user-1")).thenReturn(List.of());
        when(sentencePoolRepo.findAllByVocabularyIdOrderByCreatedAtAsc("v-1")).thenReturn(List.of());

        when(vocabularyClozeLlmApi.generateClozeSentences(eq("Practice"), anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            List<VocabularyClozeGenerationSeed> seeds = invocation.getArgument(1);
            System.out.println("STEP 2: LLM called with seeds = " + seeds.stream().map(VocabularyClozeGenerationSeed::surface).toList());
            return List.of(new VocabularyClozeSentenceResult(
                    "gehen",
                    "Ich ____ jeden Tag zur Arbeit.",
                    "go",
                    "gehe",
                    List.of("gehe"),
                    "I go to work every day."
            ));
        });

        when(sentencePoolRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usageRepo.findByUserIdAndSentenceId(eq("user-1"), anyString())).thenReturn(Optional.empty());
        when(usageRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessionRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<StudySessionItemEntity> storedItems = new ArrayList<>();
        when(itemRepo.save(any())).thenAnswer(invocation -> {
            StudySessionItemEntity item = invocation.getArgument(0);
            storedItems.add(item);
            return item;
        });
        when(itemRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc(anyString())).thenAnswer(invocation -> storedItems);
        when(sentencePoolRepo.findById(anyString())).thenAnswer(invocation -> {
            String sentenceId = invocation.getArgument(0);
            var e = new StudySentencePoolEntity();
            e.setId(sentenceId);
            e.setVocabularyId("v-1");
            e.setSentenceTextWithBlank("Ich ____ jeden Tag zur Arbeit.");
            e.setTrueAnswerSurface("gehe");
            e.setNormalizedTrueAnswer("gehe");
            e.setHint("go");
            e.setSource("LLM");
            e.setCreatedAt(Instant.now());
            return Optional.of(e);
        });

        System.out.println("STEP 3: Execute createSession");
        var response = service.createSession("user-1", 10);

        System.out.println("STEP 4: Response sessionId = " + response.sessionId());
        System.out.println("STEP 5: Response status/rated/total = " + response.status() + "/" + response.ratedCount() + "/" + response.totalCount());
        System.out.println("STEP 6: Current item sentence = " + (response.currentItem() == null ? "<none>" : response.currentItem().clozeSentence()));

        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.currentItem()).isNotNull();
        assertThat(response.currentItem().expectedAnswer()).isEqualTo("gehe");
    }

    @Test
    @DisplayName("submitAnswer: non-exact answer uses LLM feedback and applies mapped HARD rating")
    void submitAnswerUsesLlmAndMapsRating() {
        System.out.println("STEP 1: Arrange active session with one unrated item");

        var session = new StudySessionEntity();
        session.setId("s-1");
        session.setUserId("user-1");
        session.setStatus("ACTIVE");
        session.setCreatedAt(Instant.now());

        var item = new StudySessionItemEntity();
        item.setId("i-1");
        item.setSessionId("s-1");
        item.setFlashcardId("f-1");
        item.setVocabularyId("v-1");
        item.setSentenceId("sen-1");
        item.setQueueRankSnapshot(0);
        item.setPresentedAt(Instant.now());

        var sentence = new StudySentencePoolEntity();
        sentence.setId("sen-1");
        sentence.setVocabularyId("v-1");
        sentence.setSentenceTextWithBlank("Ich ____ jeden Tag zur Arbeit.");
        sentence.setTrueAnswerSurface("gehe");
        sentence.setNormalizedTrueAnswer("gehe");
        sentence.setHint("go");
        sentence.setSource("LLM");
        sentence.setCreatedAt(Instant.now());

        when(sessionRepo.findById("s-1")).thenReturn(Optional.of(session));
        when(itemRepo.findByIdAndSessionId("i-1", "s-1")).thenReturn(Optional.of(item));
        when(sentencePoolRepo.findById("sen-1")).thenReturn(Optional.of(sentence));

        when(studyAnswerEvaluationLlmApi.evaluate(anyString(), anyString(), anyString(), anyString(), eq("gehen")))
                .thenAnswer(invocation -> {
                    System.out.println("STEP 2: LLM evaluation called for answer = gehen");
                    return new StudyAnswerEvaluationResult(0.70, 0.60, 0.91, "Close meaning, but verb form should match the sentence.");
                });

        when(itemRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usageRepo.findByUserIdAndSentenceId("user-1", "sen-1")).thenReturn(Optional.empty());
        when(answerLogRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<StudySessionItemEntity> sessionItems = new ArrayList<>();
        sessionItems.add(item);
        when(itemRepo.findAllBySessionIdOrderByQueueRankSnapshotAsc("s-1")).thenAnswer(invocation -> sessionItems);

        System.out.println("STEP 3: Execute submitAnswer with non-exact answer");
        var response = service.submitAnswer("s-1", "i-1", "user-1", "gehen");

        System.out.println("STEP 4: Feedback = " + response.feedback());
        System.out.println("STEP 5: Applied rating = " + response.appliedRating());
        System.out.println("STEP 6: Session status = " + response.status());

        verify(reviewVocabularyFlashcardApi).reviewVocabularyFlashcard("f-1", Rating.HARD);
        assertThat(response.appliedRating()).isEqualTo(Rating.HARD);
        assertThat(response.feedback()).contains("Close meaning");
    }

    private Vocabulary vocab(String id, String surface, String translation) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId("user-1"),
                surface,
                translation,
                Vocabulary.EntryKind.WORD,
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
