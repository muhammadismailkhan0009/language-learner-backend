package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationCandidate;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InvalidWordPracticeBatchException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;

class WordPracticeMcpToolsTests {
    private final StubJobService jobService = new StubJobService();
    private final StubGenerationService generationService = new StubGenerationService();
    private final WordPracticeMcpTools tools = new WordPracticeMcpTools(jobService, generationService);

    @Test
    void fetchReturnsEmptyWhenNoWordPracticeJobExists() {
        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getWordPracticeGeneration()).isEmpty();
        }

        assertThat(generationService.prepared).isFalse();
    }

    @Test
    void fetchReturnsCompleteGenerationPromptForPendingJob() {
        jobService.exists = true;
        generationService.preparedPrompt = "complete Word Practice prompt";

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            assertThat(tools.getWordPracticeGeneration()).isEqualTo("complete Word Practice prompt");
        }
    }

    @Test
    void successfulStorePassesEchoedSelectionAndGeneratedGroupsToService() {
        var candidate = candidate();
        var group = group(candidate.vocabularyId());
        var generation = new WordPracticeGeneration(
                List.of(candidate.selection()),
                List.of(group)
        );
        generationService.storedCount = 3;

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeWordPracticeGeneration(generation);
            assertThat(result.stored()).isTrue();
            assertThat(result.storedPracticeCount()).isEqualTo(3);
            assertThat(result.validationErrors()).isEmpty();
        }

        assertThat(generationService.storedGeneration).isEqualTo(generation);
    }

    @Test
    void validationFailureReturnsErrorAndLeavesJobForRetry() {
        var generation = new WordPracticeGeneration(List.of(), List.of());
        generationService.storeFailure = new InvalidWordPracticeBatchException(
                "exactly 10 vocabulary groups are required");

        try (var ignored = McpUserContextHolder.scoped("user-1")) {
            var result = tools.storeWordPracticeGeneration(generation);
            assertThat(result.stored()).isFalse();
            assertThat(result.storedPracticeCount()).isZero();
            assertThat(result.validationErrors()).containsExactly("exactly 10 vocabulary groups are required");
        }

        assertThat(jobService.deleted).isFalse();
    }

    private WordPracticeGenerationCandidate candidate() {
        return new WordPracticeGenerationCandidate(
                new WordPracticeCandidate("vocabulary-1", WordPracticeSelectionCategory.NEW),
                "unterschreiben",
                "to sign"
        );
    }

    private GeneratedWordPracticeGroup group(String vocabularyId) {
        return new GeneratedWordPracticeGroup(vocabularyId, List.of(
                practice(vocabularyId, 1),
                practice(vocabularyId, 2),
                practice(vocabularyId, 3)
        ));
    }

    private GeneratedWordPractice practice(String vocabularyId, int context) {
        return new GeneratedWordPractice(
                vocabularyId,
                context == 1 ? WordPracticeDirection.GERMAN_TO_ENGLISH : WordPracticeDirection.ENGLISH_TO_GERMAN,
                "source " + context,
                "cloze _____ " + context,
                "complete " + context,
                "answer " + context,
                List.of()
        );
    }

    private static final class StubJobService extends ContentGenerationJobService {
        private boolean exists;
        private boolean deleted;

        private StubJobService() {
            super(null);
        }

        @Override
        public boolean exists(String userId,
                              com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType type) {
            return exists;
        }

        @Override
        public void delete(String userId,
                           com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType type) {
            deleted = true;
        }
    }

    private static final class StubGenerationService extends WordPracticeGenerationService {
        private boolean prepared;
        private String preparedPrompt = "";
        private WordPracticeGeneration storedGeneration;
        private int storedCount;
        private RuntimeException storeFailure;

        private StubGenerationService() {
            super(null, null, null, null, 5);
        }

        @Override
        public String prepareGenerationPrompt(String userId) {
            prepared = true;
            return preparedPrompt;
        }

        @Override
        public int store(String userId, List<WordPracticeCandidate> selected,
                         List<GeneratedWordPracticeGroup> groups) {
            if (storeFailure != null) throw storeFailure;
            storedGeneration = new WordPracticeGeneration(selected, groups);
            return storedCount;
        }
    }
}
