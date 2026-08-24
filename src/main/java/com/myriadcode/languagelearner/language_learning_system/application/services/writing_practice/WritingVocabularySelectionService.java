package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary.RecentExerciseVocabularyUsageService;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticePolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WritingVocabularySelectionService {
    private final FetchVocabularyFlashcardReviewsApi reviewsApi;
    private final FetchPrivateVocabularyApi vocabularyApi;
    private final PracticeVocabularyReferenceRepo referenceRepo;
    private final RecentExerciseVocabularyUsageService recentUsageService;
    private final WritingPracticeCandidateAssembler assembler = new WritingPracticeCandidateAssembler();
    private final WritingPracticePolicy policy = new WritingPracticePolicy();

    public WritingVocabularySelectionService(FetchVocabularyFlashcardReviewsApi reviewsApi,
                                             FetchPrivateVocabularyApi vocabularyApi,
                                             PracticeVocabularyReferenceRepo referenceRepo,
                                             RecentExerciseVocabularyUsageService recentUsageService) {
        this.reviewsApi = reviewsApi;
        this.vocabularyApi = vocabularyApi;
        this.referenceRepo = referenceRepo;
        this.recentUsageService = recentUsageService;
    }

    public List<PrivateVocabularyRecord> select(String userId, Integer requestedLimit) {
        var reviews = reviewsApi.getVocabularyFlashcardsByUser(userId);
        if (reviews.isEmpty()) throw new IllegalArgumentException("No vocabulary flashcards found for user");
        var references = referenceRepo.findByUserId(userId);
        if (references.isEmpty()) throw new IllegalArgumentException("No practice vocabulary references found for user");
        var ids = references.stream().map(ref -> ref.vocabularyId().id()).collect(Collectors.toSet());
        var practiceReviews = assembler.filterByPracticeVocabulary(reviews, ids);
        if (practiceReviews.isEmpty()) throw new IllegalArgumentException("No flashcards found for practice vocabulary references");
        var vocabularyIds = practiceReviews.stream().map(VocabularyFlashcardReviewRecord::vocabularyId).distinct().toList();
        var records = vocabularyApi.getVocabularyRecords(vocabularyIds, userId).stream()
                .filter(record -> userId.equals(record.userId()))
                .collect(Collectors.toMap(PrivateVocabularyRecord::id, Function.identity()));
        var candidates = assembler.buildCandidates(practiceReviews, records);
        if (candidates.isEmpty()) throw new IllegalArgumentException("No vocabulary candidates found for writing practice");
        var counts = recentUsageService == null ? Map.<String, Integer>of() : recentUsageService.countRecentSessionUsage(userId);
        var selected = policy.selectCandidates(candidates, Instant.now(), counts);
        if (selected.isEmpty()) throw new IllegalArgumentException("Unable to select vocabulary for writing practice");
        int limit = requestedLimit == null ? selected.size() : Math.max(1, Math.min(requestedLimit, selected.size()));
        return selected.subList(0, limit).stream().map(candidate -> records.get(candidate.vocabularyId())).toList();
    }
}
