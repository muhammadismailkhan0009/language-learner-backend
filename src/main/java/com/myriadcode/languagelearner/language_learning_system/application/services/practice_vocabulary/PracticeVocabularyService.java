package com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary;

import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class PracticeVocabularyService {

    private final VocabularyRepo vocabularyRepo;
    private final ReadingPracticeLlmApi readingPracticeLlmApi;
    private final PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo;

    public PracticeVocabularyService(VocabularyRepo vocabularyRepo,
                                     ReadingPracticeLlmApi readingPracticeLlmApi,
                                     PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo) {
        this.vocabularyRepo = vocabularyRepo;
        this.readingPracticeLlmApi = readingPracticeLlmApi;
        this.practiceVocabularyReferenceRepo = practiceVocabularyReferenceRepo;
    }

    public void enqueueExtraction(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        Vals.runIo(() -> {
            try {
                extractAndStore(userId, text);
            } catch (RuntimeException ex) {
                log.error("Practice vocabulary extraction failed for userId={}", userId, ex);
            }
        });
    }

    public ExtractPracticeVocabularyResult extractAndStore(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }

        var userVocabulary = vocabularyRepo.findByUserId(userId);
        if (userVocabulary.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary found for user");
        }

        var seeds = userVocabulary.stream()
                .map(vocabulary -> new ReadingPracticeVocabularySeed(vocabulary.surface(), vocabulary.translation()))
                .toList();

        List<String> usedSurfaces;
        try (var ignored = LlmUserContextHolder.scoped(userId)) {
            usedSurfaces = readingPracticeLlmApi.identifyUsedVocabulary(seeds, text);
        }

        var vocabularyBySurface = new LinkedHashMap<String, com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary>();
        for (var vocabulary : userVocabulary) {
            if (vocabulary.surface() == null || vocabulary.surface().isBlank()) {
                continue;
            }
            vocabularyBySurface.putIfAbsent(vocabulary.surface().trim(), vocabulary);
        }

        var matchedWords = new ArrayList<String>();
        var matchedVocabularyIds = new ArrayList<String>();
        var seenVocabularyIds = new HashSet<String>();
        for (var surface : usedSurfaces) {
            if (surface == null || surface.isBlank()) {
                continue;
            }
            var normalized = surface.trim();
            var vocabulary = vocabularyBySurface.get(normalized);
            if (vocabulary == null) {
                continue;
            }
            var vocabularyId = vocabulary.id().id();
            if (!seenVocabularyIds.add(vocabularyId)) {
                continue;
            }
            matchedWords.add(vocabulary.surface());
            matchedVocabularyIds.add(vocabularyId);
        }

        int added = 0;
        int existing = 0;
        var now = Instant.now();
        for (var vocabularyId : matchedVocabularyIds) {
            var maybeExisting = practiceVocabularyReferenceRepo.findByUserIdAndVocabularyId(userId, vocabularyId);
            if (maybeExisting.isPresent()) {
                var current = maybeExisting.get();
                practiceVocabularyReferenceRepo.save(new PracticeVocabularyReference(
                        current.id(),
                        current.userId(),
                        current.vocabularyId(),
                        current.timesMatched() + 1,
                        current.createdAt(),
                        now
                ));
                existing++;
                continue;
            }
            practiceVocabularyReferenceRepo.save(new PracticeVocabularyReference(
                    new PracticeVocabularyReference.PracticeVocabularyReferenceId(UUID.randomUUID().toString()),
                    new UserId(userId),
                    new com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary.VocabularyId(vocabularyId),
                    1,
                    now,
                    now
            ));
            added++;
        }

        return new ExtractPracticeVocabularyResult(added, existing, matchedWords, matchedVocabularyIds);
    }
}
