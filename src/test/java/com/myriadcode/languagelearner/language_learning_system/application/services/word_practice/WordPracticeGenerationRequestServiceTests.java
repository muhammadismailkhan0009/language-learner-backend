package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeGenerationRequestServiceTests {

    @Test
    void createsJobWhenFiveVocabularyWordsAreActive() {
        var jobs = new RecordingJobService();
        var service = new WordPracticeGenerationRequestService(new StubRepo(5), jobs);

        assertThat(service.request("user-1")).contains("Run your MCP tool");
        assertThat(jobs.createdType).isEqualTo(WORD_PRACTICE);
    }

    @Test
    void rejectsRequestWhenSixVocabularyWordsAreActive() {
        var jobs = new RecordingJobService();
        var service = new WordPracticeGenerationRequestService(new StubRepo(6), jobs);

        assertThatThrownBy(() -> service.request("user-1"))
                .hasMessage("Word Practice capacity exceeded");
        assertThat(jobs.createdType).isNull();
    }

    private static final class RecordingJobService extends ContentGenerationJobService {
        private com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType createdType;

        private RecordingJobService() {
            super(null);
        }

        @Override
        public com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob createOrReplace(
                String userId,
                com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType type) {
            createdType = type;
            return null;
        }
    }

    private record StubRepo(int activeCount) implements WordPracticeRepo {
        @Override public int countDistinctActiveVocabulary(String userId) { return activeCount; }
        @Override public Set<String> findActiveVocabularyIds(String userId) { return Set.of(); }
        @Override public List<WordPractice> findByUserId(String userId) { return List.of(); }
        @Override public Optional<WordPractice> findByIdAndUserId(String practiceId, String userId) { return Optional.empty(); }
        @Override public void deleteByIdAndUserId(String practiceId, String userId) { }
        @Override public void saveGeneration(String userId, List<GeneratedWordPracticeGroup> groups, Instant createdAt) { }
    }
}
