package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.WordPracticeCandidateProvider;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJob;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.WORD_PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeGenerationServiceTests {
    private static final Instant NOW = Instant.parse("2026-09-08T10:00:00Z");
    private final RecordingRepo practiceRepo = new RecordingRepo();
    private final RecordingCandidateProvider candidateProvider = new RecordingCandidateProvider();
    private final RecordingJobService jobService = new RecordingJobService();
    private final WordPracticeGenerationService service = new WordPracticeGenerationService(
            practiceRepo, candidateProvider, jobService, userId -> com.myriadcode.languagelearner.common.enums.LanguageLevel.B1,
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void preparesAuthoritativeTenWordSelectionWithoutPersistingGenerationState() {
        var selected = service.prepare("user-1");

        assertThat(selected).hasSize(10);
        assertThat(selected).extracting(WordPracticeGenerationCandidate::vocabularyId)
                .doesNotHaveDuplicates();
    }

    @Test
    void preparesCompleteGenerationPromptWithVocabularyDataAndStoreContract() {
        var prompt = service.prepareGenerationPrompt("user-1");

        assertThat(prompt)
                .contains("Vocabulary data:")
                .contains("CEFR level B1")
                .contains("\"selection\":{\"vocabularyId\":")
                .contains("\"german\":\"de-")
                .contains("\"english\":\"en-")
                .contains("exactly 10 groups")
                .contains("3 to 5 practices")
                .contains("GERMAN_TO_ENGLISH", "ENGLISH_TO_GERMAN")
                .contains("Echo supplied selection objects exactly")
                .contains("store_word_practice_generation");
    }

    @Test
    void storesGeneratedPracticesThroughAtomicRepoOperation() {
        var selected = service.prepare("user-1");

        int stored = service.store("user-1", selections(selected), generatedGroups(selected));

        assertThat(stored).isEqualTo(30);
        assertThat(practiceRepo.saved).isTrue();
        assertThat(jobService.deleted).isTrue();
    }

    @Test
    void rechecksCapacityBeforeStoring() {
        var selected = service.prepare("user-1");
        practiceRepo.activeCount = 6;

        assertThatThrownBy(() -> service.store("user-1", selections(selected), generatedGroups(selected)))
                .hasMessage("Word Practice capacity exceeded");
        assertThat(practiceRepo.saved).isFalse();
        assertThat(jobService.deleted).isFalse();
    }

    private List<WordPracticeCandidate> selections(List<WordPracticeGenerationCandidate> selected) {
        return selected.stream().map(WordPracticeGenerationCandidate::selection).toList();
    }

    private List<GeneratedWordPracticeGroup> generatedGroups(List<WordPracticeGenerationCandidate> selected) {
        var groups = new ArrayList<GeneratedWordPracticeGroup>();
        for (int index = 0; index < selected.size(); index++) {
            String id = selected.get(index).vocabularyId();
            var direction = index == 0 ? WordPracticeDirection.GERMAN_TO_ENGLISH : WordPracticeDirection.ENGLISH_TO_GERMAN;
            var practices = new ArrayList<GeneratedWordPractice>();
            for (int context = 1; context <= 3; context++) {
                practices.add(new GeneratedWordPractice(id, direction, "source " + id + " " + context,
                        "cloze " + id + " _____ " + context, "complete " + id + " " + context,
                        "answer " + context, List.of()));
            }
            groups.add(new GeneratedWordPracticeGroup(id, practices));
        }
        return groups;
    }

    private static final class RecordingCandidateProvider implements WordPracticeCandidateProvider {
        @Override
        public Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> findRankedCandidates(String userId) {
            var candidates = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>>(
                    WordPracticeSelectionCategory.class);
            candidates.put(WordPracticeSelectionCategory.NEW, candidates(WordPracticeSelectionCategory.NEW, 1, 5));
            candidates.put(WordPracticeSelectionCategory.LEARNING,
                    candidates(WordPracticeSelectionCategory.LEARNING, 1, 5));
            return candidates;
        }

        private List<WordPracticeGenerationCandidate> candidates(WordPracticeSelectionCategory category,
                                                                  int start, int count) {
            var result = new ArrayList<WordPracticeGenerationCandidate>();
            for (int number = start; number < start + count; number++) {
                String prefix = category.name().toLowerCase(Locale.ROOT);
                var selection = new WordPracticeCandidate(prefix + "-" + number, category);
                result.add(new WordPracticeGenerationCandidate(selection, "de-" + prefix + number, "en-" + prefix + number));
            }
            return result;
        }
    }

    private static final class RecordingJobService extends ContentGenerationJobService {
        private boolean deleted;
        private RecordingJobService() { super(null); }
        @Override public ContentGenerationJob require(String userId, ContentGenerationJobType type) {
            return new ContentGenerationJob(userId, type, NOW);
        }
        @Override public void delete(String userId, ContentGenerationJobType type) { deleted = true; }
    }

    private static final class RecordingRepo implements WordPracticeRepo {
        private int activeCount;
        private boolean saved;
        @Override public int countDistinctActiveVocabulary(String userId) { return activeCount; }
        @Override public Set<String> findActiveVocabularyIds(String userId) { return Set.of(); }
        @Override public List<WordPractice> findByUserId(String userId) { return List.of(); }
        @Override public Optional<WordPractice> findByIdAndUserId(String practiceId, String userId) { return Optional.empty(); }
        @Override public void deleteByIdAndUserId(String practiceId, String userId) { }
        @Override public void saveGeneration(String userId, List<GeneratedWordPracticeGroup> groups, Instant createdAt) {
            saved = true;
        }
    }
}
