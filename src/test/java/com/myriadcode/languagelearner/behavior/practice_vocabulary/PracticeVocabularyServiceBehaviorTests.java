package com.myriadcode.languagelearner.behavior.practice_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary.PracticeVocabularyService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PracticeVocabularyServiceBehaviorTests {

    private final VocabularyRepo vocabularyRepo = mock(VocabularyRepo.class);
    private final ReadingPracticeLlmApi readingPracticeLlmApi = mock(ReadingPracticeLlmApi.class);
    private final PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo = mock(PracticeVocabularyReferenceRepo.class);

    private final PracticeVocabularyService service = new PracticeVocabularyService(
            vocabularyRepo,
            readingPracticeLlmApi,
            practiceVocabularyReferenceRepo
    );

    @Test
    @DisplayName("extractAndStore: stores unique matched vocabulary references from LLM surfaces")
    void extractAndStoreStoresUniqueReferences() {
        when(vocabularyRepo.findByUserId("user-1")).thenReturn(List.of(
                vocab("v-1", "gehen"),
                vocab("v-2", "kennen")
        ));
        when(readingPracticeLlmApi.identifyUsedVocabulary(any(), eq("song text"))).thenReturn(List.of("gehen", "gehen", "kennen", "unknown"));
        when(practiceVocabularyReferenceRepo.findByUserIdAndVocabularyId("user-1", "v-1")).thenReturn(Optional.empty());
        when(practiceVocabularyReferenceRepo.findByUserIdAndVocabularyId("user-1", "v-2")).thenReturn(Optional.empty());
        when(practiceVocabularyReferenceRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.extractAndStore("user-1", "song text");

        assertThat(response.addedCount()).isEqualTo(2);
        assertThat(response.existingCount()).isEqualTo(0);
        assertThat(response.matchedWords()).containsExactly("gehen", "kennen");
        assertThat(response.vocabularyIds()).containsExactly("v-1", "v-2");
        verify(practiceVocabularyReferenceRepo, times(2)).save(any());
    }

    @Test
    @DisplayName("extractAndStore: increments existing references instead of inserting duplicates")
    void extractAndStoreIncrementsExistingReferences() {
        when(vocabularyRepo.findByUserId("user-1")).thenReturn(List.of(vocab("v-1", "gehen")));
        when(readingPracticeLlmApi.identifyUsedVocabulary(any(), eq("text"))).thenReturn(List.of("gehen"));
        var existing = new PracticeVocabularyReference(
                new PracticeVocabularyReference.PracticeVocabularyReferenceId("ref-1"),
                new UserId("user-1"),
                new Vocabulary.VocabularyId("v-1"),
                2,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
        when(practiceVocabularyReferenceRepo.findByUserIdAndVocabularyId("user-1", "v-1")).thenReturn(Optional.of(existing));
        when(practiceVocabularyReferenceRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.extractAndStore("user-1", "text");

        assertThat(response.addedCount()).isEqualTo(0);
        assertThat(response.existingCount()).isEqualTo(1);
        verify(practiceVocabularyReferenceRepo).save(argThat(reference ->
                reference.id().id().equals("ref-1")
                        && reference.timesMatched() == 3
                        && reference.vocabularyId().id().equals("v-1")
        ));
    }

    private Vocabulary vocab(String id, String surface) {
        return new Vocabulary(
                new Vocabulary.VocabularyId(id),
                new UserId("user-1"),
                surface,
                "translation-" + id,
                Vocabulary.EntryKind.WORD,
                null,
                List.of(),
                null,
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}
