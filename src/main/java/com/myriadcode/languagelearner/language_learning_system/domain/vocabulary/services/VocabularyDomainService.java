package com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class VocabularyDomainService {

    private VocabularyDomainService() {
    }

    public static Vocabulary create(UserId userId,
                                    String surface,
                                    String translation,
                                    Vocabulary.EntryKind entryKind,
                                    String notes,
                                    List<VocabularyExampleSentence> exampleSentences) {
        validateUserId(userId);
        validateSurface(surface);
        validateTranslation(translation);
        validateEntryKind(entryKind);

        return new Vocabulary(
                new Vocabulary.VocabularyId(UUID.randomUUID().toString()),
                userId,
                surface,
                translation,
                entryKind,
                normalizeNotes(notes),
                normalizeNewExamples(exampleSentences)
        );
    }

    public static Vocabulary edit(Vocabulary existing,
                                  String surface,
                                  String translation,
                                  Vocabulary.EntryKind entryKind,
                                  String notes,
                                  List<VocabularyExampleSentence> exampleSentenceUpdates) {
        var updatedSurface = surface == null ? existing.surface() : surface;
        var updatedTranslation = translation == null ? existing.translation() : translation;
        var updatedEntryKind = entryKind == null ? existing.entryKind() : entryKind;
        var updatedNotes = notes == null ? existing.notes() : normalizeNotes(notes);

        validateSurface(updatedSurface);
        validateTranslation(updatedTranslation);
        validateEntryKind(updatedEntryKind);

        return new Vocabulary(
                existing.id(),
                existing.userId(),
                updatedSurface,
                updatedTranslation,
                updatedEntryKind,
                updatedNotes,
                mergeExampleUpdates(existing.exampleSentences(), exampleSentenceUpdates)
        );
    }

    private static List<VocabularyExampleSentence> normalizeNewExamples(List<VocabularyExampleSentence> sentences) {
        if (sentences == null || sentences.isEmpty()) {
            throw new IllegalArgumentException("Vocabulary must contain at least one example sentence");
        }

        var normalized = new ArrayList<VocabularyExampleSentence>();
        for (VocabularyExampleSentence sentence : sentences) {
            validateSentenceAndTranslation(sentence.sentence(), sentence.translation());
            normalized.add(new VocabularyExampleSentence(
                    new VocabularyExampleSentence.VocabularyExampleSentenceId(UUID.randomUUID().toString()),
                    sentence.sentence(),
                    sentence.translation()
            ));
        }
        return normalized;
    }

    private static List<VocabularyExampleSentence> mergeExampleUpdates(
            List<VocabularyExampleSentence> existingSentences,
            List<VocabularyExampleSentence> updates
    ) {
        if (updates == null || updates.isEmpty()) {
            if (existingSentences == null || existingSentences.isEmpty()) {
                throw new IllegalArgumentException("Vocabulary must contain at least one example sentence");
            }
            return existingSentences;
        }

        // Provided update list is authoritative for sentence set: omitted sentences are removed.
        var merged = new ArrayList<VocabularyExampleSentence>();

        for (VocabularyExampleSentence update : updates) {
            validateSentenceAndTranslation(update.sentence(), update.translation());
            if (update.id() == null || update.id().id() == null || update.id().id().isBlank()) {
                merged.add(new VocabularyExampleSentence(
                        new VocabularyExampleSentence.VocabularyExampleSentenceId(UUID.randomUUID().toString()),
                        update.sentence(),
                        update.translation()
                ));
                continue;
            }

            var updated = false;
            for (VocabularyExampleSentence existing : existingSentences) {
                if (existing.id().id().equals(update.id().id())) {
                    merged.add(new VocabularyExampleSentence(
                            existing.id(),
                            update.sentence(),
                            update.translation()
                    ));
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                throw new IllegalArgumentException("Example sentence not found in vocabulary: " + update.id().id());
            }
        }

        if (merged.isEmpty()) {
            throw new IllegalArgumentException("Vocabulary must contain at least one example sentence");
        }

        return merged;
    }

    private static String normalizeNotes(String notes) {
        if (notes == null) {
            return null;
        }
        var trimmed = notes.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validateSurface(String surface) {
        if (surface == null || surface.isBlank()) {
            throw new IllegalArgumentException("Vocabulary surface is required");
        }
    }

    private static void validateUserId(UserId userId) {
        if (userId == null || userId.id() == null || userId.id().isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
    }

    private static void validateTranslation(String translation) {
        if (translation == null || translation.isBlank()) {
            throw new IllegalArgumentException("Vocabulary translation is required");
        }
    }

    private static void validateEntryKind(Vocabulary.EntryKind entryKind) {
        if (entryKind == null) {
            throw new IllegalArgumentException("Vocabulary entry kind is required");
        }
    }

    private static void validateSentenceAndTranslation(String sentence, String translation) {
        if (sentence == null || sentence.isBlank()) {
            throw new IllegalArgumentException("Example sentence is required");
        }
        if (translation == null || translation.isBlank()) {
            throw new IllegalArgumentException("Example sentence translation is required");
        }
    }
}
