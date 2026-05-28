package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.services.WritingPracticeCandidate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class WritingPracticeCandidateAssembler {

    List<VocabularyFlashcardReviewRecord> filterByPracticeVocabulary(
            List<VocabularyFlashcardReviewRecord> flashcards,
            Set<String> practiceVocabularyIds
    ) {
        if (flashcards == null || flashcards.isEmpty() || practiceVocabularyIds == null || practiceVocabularyIds.isEmpty()) {
            return List.of();
        }
        return flashcards.stream()
                .filter(card -> practiceVocabularyIds.contains(card.vocabularyId()))
                .toList();
    }

    List<WritingPracticeCandidate> buildCandidates(
            List<VocabularyFlashcardReviewRecord> flashcards,
            Map<String, PrivateVocabularyRecord> vocabRecords
    ) {
        if (flashcards == null || flashcards.isEmpty() || vocabRecords == null || vocabRecords.isEmpty()) {
            return List.of();
        }
        return flashcards.stream()
                .filter(VocabularyFlashcardReviewRecord::isReversed)
                .map(review -> {
                    var vocab = vocabRecords.get(review.vocabularyId());
                    if (vocab == null) {
                        return null;
                    }
                    var createdAt = vocab.createdAt() == null ? Instant.EPOCH : vocab.createdAt();
                    return new WritingPracticeCandidate(
                            review.flashcardId(),
                            review.vocabularyId(),
                            review.fsrsState(),
                            createdAt,
                            review.due(),
                            review.retrievability(),
                            review.lapses(),
                            review.lastReview()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    List<WritingPracticeVocabularySeed> toVocabularySeeds(
            List<WritingPracticeCandidate> selected,
            Map<String, PrivateVocabularyRecord> vocabRecords
    ) {
        if (selected == null || selected.isEmpty() || vocabRecords == null || vocabRecords.isEmpty()) {
            return List.of();
        }
        return selected.stream()
                .map(candidate -> vocabRecords.get(candidate.vocabularyId()))
                .filter(record -> record != null)
                .map(record -> new WritingPracticeVocabularySeed(record.surface(), record.translation()))
                .toList();
    }

    List<WritingVocabularyUsage> buildUsages(
            List<WritingPracticeCandidate> selected,
            Map<String, PrivateVocabularyRecord> vocabRecords,
            Set<String> usedVocabularySurfaces
    ) {
        if (selected == null || selected.isEmpty() || usedVocabularySurfaces == null || usedVocabularySurfaces.isEmpty()) {
            return List.of();
        }
        return selected.stream()
                .filter(candidate -> {
                    var vocab = vocabRecords.get(candidate.vocabularyId());
                    return vocab != null && usedVocabularySurfaces.contains(normalizeSurface(vocab.surface()));
                })
                .collect(Collectors.toMap(
                        WritingPracticeCandidate::flashCardId,
                        candidate -> new WritingVocabularyUsage(
                                new WritingVocabularyUsage.WritingVocabularyUsageId(UUID.randomUUID().toString()),
                                candidate.flashCardId(),
                                candidate.vocabularyId()
                        ),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    private String normalizeSurface(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}

