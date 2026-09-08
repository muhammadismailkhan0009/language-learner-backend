package com.myriadcode.languagelearner.language_learning_system.infra.jpa.word_practice;

import com.myriadcode.fsrs.api.enums.ReviewLogRating;
import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.dao.repos.FlashCardReviewJpaRepo;
import com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities.FlashCardReviewLogValue;
import com.myriadcode.languagelearner.language_learning_system.application.externals.WordPracticeCandidateProvider;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.language_learning_system.application.services.word_practice.WordPracticeGenerationCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeSelectionCategory;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.entities.VocabularyEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.vocabulary.repos.VocabularyEntityJpaRepo;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
class WordPracticeCandidateJpaAdapter implements WordPracticeCandidateProvider {
    private final VocabularyEntityJpaRepo vocabularies;
    private final WordPracticeJpaRepository practices;
    private final FlashCardReviewJpaRepo reviews;
    private final ConsumedWeakEventJpaRepository consumedWeakEvents;
    private final RecentExerciseVocabularyUsageService recentUsage;

    WordPracticeCandidateJpaAdapter(VocabularyEntityJpaRepo vocabularies,
                                    WordPracticeJpaRepository practices,
                                    FlashCardReviewJpaRepo reviews,
                                    ConsumedWeakEventJpaRepository consumedWeakEvents,
                                    RecentExerciseVocabularyUsageService recentUsage) {
        this.vocabularies = vocabularies; this.practices = practices; this.reviews = reviews;
        this.consumedWeakEvents = consumedWeakEvents; this.recentUsage = recentUsage;
    }

    public Map<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>> findRankedCandidates(
            String userId, int lowExposureMax) {
        var vocabulary = vocabularies.findAllByUserId(userId);
        var activeVocabulary = practices.findActiveVocabularyIds(userId);
        var exposure = recentUsage.countRecentSessionUsage(userId);
        var weak = currentUnconsumedWeakEvents(userId);
        var result = new EnumMap<WordPracticeSelectionCategory, List<WordPracticeGenerationCandidate>>(
                WordPracticeSelectionCategory.class);
        result.put(WordPracticeSelectionCategory.NEW, vocabulary.stream()
                .filter(v -> !activeVocabulary.contains(v.getId()))
                .sorted(Comparator.comparing(VocabularyEntity::getCreatedAt).thenComparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.NEW, null)).toList());
        result.put(WordPracticeSelectionCategory.WEAK, vocabulary.stream().filter(v -> weak.containsKey(v.getId()))
                .sorted(Comparator.comparing((VocabularyEntity v) -> weak.get(v.getId()).review()).reversed()
                        .thenComparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.WEAK, weak.get(v.getId()).eventId())).toList());
        result.put(WordPracticeSelectionCategory.LOW_EXPOSURE, vocabulary.stream()
                .filter(v -> exposure.getOrDefault(v.getId(), 0) < lowExposureMax)
                .sorted(Comparator.comparingInt((VocabularyEntity v) -> exposure.getOrDefault(v.getId(), 0))
                        .thenComparing(VocabularyEntity::getCreatedAt).thenComparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.LOW_EXPOSURE, null)).toList());
        result.put(WordPracticeSelectionCategory.STALE, List.of());
        result.put(WordPracticeSelectionCategory.RANDOM, vocabulary.stream()
                .sorted(Comparator.comparing(VocabularyEntity::getId))
                .map(v -> candidate(v, WordPracticeSelectionCategory.RANDOM, null)).toList());
        return Map.copyOf(result);
    }

    private Map<String, WeakEvent> currentUnconsumedWeakEvents(String userId) {
        var consumed = consumedWeakEvents.findAllByUserId(userId).stream()
                .map(ConsumedWeakEventJpaRepository.WeakEventIdView::getWeakEventId).collect(java.util.stream.Collectors.toSet());
        var result = new HashMap<String, WeakEvent>();
        for (var card : reviews.findAllByContentTypeAndUserId(ContentRefType.VOCABULARY, userId)) {
            if (card.getReviewLogs() == null) continue;
            card.getReviewLogs().stream().filter(Objects::nonNull).max(Comparator.comparing(FlashCardReviewLogValue::review))
                    .filter(log -> log.rating() == ReviewLogRating.HARD || log.rating() == ReviewLogRating.AGAIN)
                    .map(log -> new WeakEvent(card.getId() + ":" + log.review() + ":" + log.rating(), log.review()))
                    .filter(event -> !consumed.contains(event.eventId()))
                    .ifPresent(event -> result.merge(card.getLanguageContentId(), event,
                            (left, right) -> left.review().isAfter(right.review()) ? left : right));
        }
        return result;
    }

    private WordPracticeGenerationCandidate candidate(VocabularyEntity value, WordPracticeSelectionCategory category,
                                                        String weakEventId) {
        return new WordPracticeGenerationCandidate(new WordPracticeCandidate(value.getId(), category, weakEventId),
                value.getSurface(), value.getTranslation());
    }
    private record WeakEvent(String eventId, Instant review) {}
}
