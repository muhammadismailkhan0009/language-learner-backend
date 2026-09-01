package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.fsrs.api.enums.State;
import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeGeneration;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary.VocabularyExtractionService;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingGenerationContext;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingGenerationContextService;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WritingPracticeVocabularyExtractionFlowTests {

    @Test
    void successfulGenerationQueuesEachSanitizedGermanParagraphAfterSessionPersistence() {
        var writingPracticeRepo = mock(WritingPracticeRepo.class);
        var flashcardsApi = mock(com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi.class);
        var privateVocabularyApi = mock(com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi.class);
        var practiceVocabularyRepo = mock(PracticeVocabularyReferenceRepo.class);
        var generationContextService = mock(WritingGenerationContextService.class);
        var recentUsageService = mock(RecentExerciseVocabularyUsageService.class);
        var extractionService = mock(VocabularyExtractionService.class);

        when(flashcardsApi.getVocabularyFlashcardsByUser("user-1")).thenReturn(List.of(
                new VocabularyFlashcardReviewRecord("card-1", "vocab-1", State.REVIEW, true)));
        when(practiceVocabularyRepo.findByUserId("user-1")).thenReturn(List.of(
                new PracticeVocabularyReference(
                        new PracticeVocabularyReference.PracticeVocabularyReferenceId("ref-1"),
                        new UserId("user-1"), new Vocabulary.VocabularyId("vocab-1"),
                        0, Instant.EPOCH, Instant.EPOCH)));
        when(privateVocabularyApi.getVocabularyRecords(List.of("vocab-1"), "user-1")).thenReturn(List.of(
                new PrivateVocabularyRecord("vocab-1", "user-1", "gehen", "to go", "WORD",
                        null, List.of(), null, Instant.EPOCH)));
        when(recentUsageService.countRecentSessionUsage("user-1")).thenReturn(Map.of());
        when(writingPracticeRepo.findRecentTopicsByUserId("user-1", 10)).thenReturn(List.of());
        when(generationContextService.build("user-1"))
                .thenReturn(new WritingGenerationContext(LanguageLevel.B1, List.of()));

        var service = new WritingPracticeService(
                writingPracticeRepo, flashcardsApi, privateVocabularyApi, practiceVocabularyRepo,
                null, generationContextService, mock(com.myriadcode.languagelearner.user_management.application.externals.UserDifficultyLevelApi.class),
                recentUsageService, extractionService);
        var generation = new WritingPracticeGeneration(List.of(
                scenario("  Ich gehe.  "),
                scenario("  Wir gehen.  "),
                scenario("  Sie gehen.  ")));

        service.storeGeneration("user-1", generation);

        var order = inOrder(writingPracticeRepo, extractionService);
        order.verify(writingPracticeRepo).save(org.mockito.ArgumentMatchers.any());
        order.verify(extractionService).submit("user-1", "Ich gehe.");
        order.verify(extractionService).submit("user-1", "Wir gehen.");
        order.verify(extractionService).submit("user-1", "Sie gehen.");
    }

    private WritingPracticeGeneration.Scenario scenario(String germanParagraph) {
        return new WritingPracticeGeneration.Scenario(
                "Topic", "I go.", germanParagraph,
                List.of(new WritingPracticeSentencePairSeed("I go.", germanParagraph)),
                List.of("gehen"));
    }
}
