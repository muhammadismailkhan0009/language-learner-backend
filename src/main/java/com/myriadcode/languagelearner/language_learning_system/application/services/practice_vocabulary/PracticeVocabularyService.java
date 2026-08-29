package com.myriadcode.languagelearner.language_learning_system.application.services.practice_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.ReadingUsedVocabularySelection;
import com.myriadcode.languagelearner.language_content.infra.llm.PromptsGenerator;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyReference;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyExtractionRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyReferenceRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class PracticeVocabularyService {

    private final VocabularyRepo vocabularyRepo;
    private final PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo;
    private final PracticeVocabularyExtractionRequestRepo extractionRequestRepo;

    public PracticeVocabularyService(VocabularyRepo vocabularyRepo,
                                     PracticeVocabularyReferenceRepo practiceVocabularyReferenceRepo,
                                     PracticeVocabularyExtractionRequestRepo extractionRequestRepo) {
        this.vocabularyRepo = vocabularyRepo;
        this.practiceVocabularyReferenceRepo = practiceVocabularyReferenceRepo;
        this.extractionRequestRepo = extractionRequestRepo;
    }

    @Deprecated(forRemoval = true)
    public String prepareExtractionPrompt(String userId) {
        requireUserId(userId);
        var request = extractionRequestRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No practice vocabulary extraction request found for user"));
        return PromptsGenerator.readingUsedVocabularySelection(loadVocabularySeeds(userId), request.text());
    }

    @Deprecated(forRemoval = true)
    public ExtractPracticeVocabularyResult storeExtraction(String userId, ReadingUsedVocabularySelection selection) {
        requireUserId(userId);
        extractionRequestRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No practice vocabulary extraction request found for user"));
        if (selection == null || selection.usedSurfaces() == null) {
            throw new PracticeVocabularyExtractionValidationException(List.of("usedSurfaces is required"));
        }
        var userVocabulary = vocabularyRepo.findByUserId(userId);
        if (userVocabulary.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary found for user");
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
        var unknownSurfaces = new ArrayList<String>();
        for (var surface : selection.usedSurfaces()) {
            if (surface == null || surface.isBlank()) {
                continue;
            }
            var normalized = surface.trim();
            var vocabulary = vocabularyBySurface.get(normalized);
            if (vocabulary == null) {
                unknownSurfaces.add(normalized);
                continue;
            }
            var vocabularyId = vocabulary.id().id();
            if (!seenVocabularyIds.add(vocabularyId)) {
                continue;
            }
            matchedWords.add(vocabulary.surface());
            matchedVocabularyIds.add(vocabularyId);
        }
        if (!unknownSurfaces.isEmpty()) {
            throw new PracticeVocabularyExtractionValidationException(
                    List.of("Unknown vocabulary surfaces: " + unknownSurfaces));
        }

        int added = 0;
        int existing = 0;
        for (var vocabularyId : matchedVocabularyIds) {
            if (recordVocabularyMatch(userId, vocabularyId)) added++;
            else existing++;
        }

        return new ExtractPracticeVocabularyResult(added, existing, matchedWords, matchedVocabularyIds);
    }

    public boolean recordVocabularyMatch(String userId, String vocabularyId) {
        requireUserId(userId);
        if (vocabularyId == null || vocabularyId.isBlank()) {
            throw new IllegalArgumentException("vocabularyId is required");
        }
        var now = Instant.now();
        var existing = practiceVocabularyReferenceRepo.findByUserIdAndVocabularyId(
                userId, vocabularyId);
        if (existing.isPresent()) {
            var current = existing.get();
            practiceVocabularyReferenceRepo.save(new PracticeVocabularyReference(
                    current.id(), current.userId(), current.vocabularyId(), current.timesMatched() + 1,
                    current.createdAt(), now));
            return false;
        }
        practiceVocabularyReferenceRepo.save(new PracticeVocabularyReference(
                new PracticeVocabularyReference.PracticeVocabularyReferenceId(UUID.randomUUID().toString()),
                new UserId(userId),
                new com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary.VocabularyId(
                        vocabularyId),
                1, now, now));
        return true;
    }

    @Deprecated(forRemoval = true)
    public void deleteExtractionRequest(String userId) {
        requireUserId(userId);
        extractionRequestRepo.deleteByUserId(userId);
    }

    private List<ReadingPracticeVocabularySeed> loadVocabularySeeds(String userId) {
        requireUserId(userId);
        var vocabulary = vocabularyRepo.findByUserId(userId);
        if (vocabulary.isEmpty()) throw new IllegalArgumentException("No vocabulary found for user");
        return vocabulary.stream().map(value -> new ReadingPracticeVocabularySeed(value.surface(), value.translation())).toList();
    }

    private void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
    }
}
