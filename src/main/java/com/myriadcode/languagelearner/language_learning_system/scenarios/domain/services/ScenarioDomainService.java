package com.myriadcode.languagelearner.language_learning_system.scenarios.domain.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.model.Scenario;
import com.myriadcode.languagelearner.language_learning_system.scenarios.domain.model.ScenarioSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ScenarioDomainService {

    private ScenarioDomainService() {
    }

    public static Scenario create(UserId userId, String nature, String targetLanguage,
                                  List<ScenarioSentence> sentences) {
        validateNature(nature);
        validateTargetLanguage(targetLanguage);
        var normalizedSentences = normalizeNewSentences(sentences);
        return new Scenario(
                new Scenario.ScenarioId(UUID.randomUUID().toString()),
                userId,
                nature,
                targetLanguage,
                normalizedSentences
        );
    }

    public static Scenario edit(Scenario existingScenario, String nature, String targetLanguage,
                                List<ScenarioSentence> sentenceUpdates) {
        var updatedNature = nature == null ? existingScenario.nature() : nature;
        var updatedTargetLanguage = targetLanguage == null ? existingScenario.targetLanguage() : targetLanguage;
        validateNature(updatedNature);
        validateTargetLanguage(updatedTargetLanguage);

        var updatedSentences = mergeSentenceUpdates(existingScenario.sentences(), sentenceUpdates);
        return new Scenario(
                existingScenario.id(),
                existingScenario.userId(),
                updatedNature,
                updatedTargetLanguage,
                updatedSentences
        );
    }

    private static List<ScenarioSentence> normalizeNewSentences(List<ScenarioSentence> sentences) {
        if (sentences == null || sentences.isEmpty()) {
            throw new IllegalArgumentException("Scenario must contain at least one sentence");
        }
        var normalized = new ArrayList<ScenarioSentence>();
        for (ScenarioSentence sentence : sentences) {
            validateSentenceAndTranslation(sentence.sentence(), sentence.translation());
            normalized.add(new ScenarioSentence(
                    new ScenarioSentence.ScenarioSentenceId(UUID.randomUUID().toString()),
                    sentence.sentence(),
                    sentence.translation()
            ));
        }
        return normalized;
    }

    private static List<ScenarioSentence> mergeSentenceUpdates(List<ScenarioSentence> existingSentences,
                                                               List<ScenarioSentence> updates) {
        if (updates == null || updates.isEmpty()) {
            if (existingSentences == null || existingSentences.isEmpty()) {
                throw new IllegalArgumentException("Scenario must contain at least one sentence");
            }
            return existingSentences;
        }

        // Provided update list is authoritative for sentence set: omitted sentences are removed.
        var merged = new ArrayList<ScenarioSentence>();
        for (ScenarioSentence update : updates) {
            validateSentenceAndTranslation(update.sentence(), update.translation());
            if (update.id() == null || update.id().id() == null || update.id().id().isBlank()) {
                merged.add(new ScenarioSentence(
                        new ScenarioSentence.ScenarioSentenceId(UUID.randomUUID().toString()),
                        update.sentence(),
                        update.translation()
                ));
                continue;
            }

            var updated = false;
            for (ScenarioSentence existing : existingSentences) {
                if (existing.id().id().equals(update.id().id())) {
                    merged.add(new ScenarioSentence(
                            existing.id(),
                            update.sentence(),
                            update.translation()
                    ));
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                throw new IllegalArgumentException("Sentence not found in scenario: " + update.id().id());
            }
        }

        if (merged.isEmpty()) {
            throw new IllegalArgumentException("Scenario must contain at least one sentence");
        }
        return merged;
    }

    private static void validateTargetLanguage(String targetLanguage) {
        if (targetLanguage == null || targetLanguage.isBlank()) {
            throw new IllegalArgumentException("Target language is required");
        }
    }

    private static void validateNature(String nature) {
        if (nature == null || nature.isBlank()) {
            throw new IllegalArgumentException("Scenario nature is required");
        }
    }

    private static void validateSentenceAndTranslation(String sentence, String translation) {
        if (sentence == null || sentence.isBlank()) {
            throw new IllegalArgumentException("Sentence is required");
        }
        if (translation == null || translation.isBlank()) {
            throw new IllegalArgumentException("Translation is required");
        }
    }
}
