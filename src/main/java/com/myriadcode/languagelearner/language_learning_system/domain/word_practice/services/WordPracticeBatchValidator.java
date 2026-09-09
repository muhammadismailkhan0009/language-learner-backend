package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.services;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions.InvalidWordPracticeBatchException;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeDirection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordPracticeBatchValidator {

    public static final int MINIMUM_PRACTICES_PER_WORD = 3;
    public static final int MAXIMUM_PRACTICES_PER_WORD = 5;

    public void validate(List<String> selectedVocabularyIds, List<GeneratedWordPracticeGroup> groups) {
        var selectedIds = requireAuthoritativeSelection(selectedVocabularyIds);
        if (groups == null || groups.size() != selectedIds.size()) {
            reject("Generated batch must contain exactly one group per selected vocabulary ID");
        }
        var generatedIds = new HashSet<String>();
        var directions = new HashSet<WordPracticeDirection>();
        for (var group : groups) {
            validateGroup(group, directions);
            if (!generatedIds.add(group.vocabularyId())) {
                reject("Generated batch contains duplicate vocabulary IDs");
            }
        }
        if (!generatedIds.equals(selectedIds)) {
            reject("Generated vocabulary IDs must match selected vocabulary IDs");
        }
        if (directions.size() != WordPracticeDirection.values().length) {
            reject("Generated batch must contain both practice directions");
        }
    }

    private Set<String> requireAuthoritativeSelection(List<String> selectedVocabularyIds) {
        if (selectedVocabularyIds == null || selectedVocabularyIds.isEmpty()
                || selectedVocabularyIds.size() > WordPracticeSelectionPolicy.BATCH_SIZE) {
            reject("Selection must contain 1 to 10 vocabulary IDs");
        }
        var selectedIds = new HashSet<String>();
        for (var vocabularyId : selectedVocabularyIds) {
            if (isBlank(vocabularyId) || !selectedIds.add(vocabularyId)) {
                reject("Selection must contain unique nonblank vocabulary IDs");
            }
        }
        return selectedIds;
    }

    private void validateGroup(GeneratedWordPracticeGroup group, Set<WordPracticeDirection> directions) {
        if (group == null || isBlank(group.vocabularyId())) {
            reject("Generated vocabulary group must identify vocabulary");
        }
        var practices = group.practices();
        if (practices.size() < MINIMUM_PRACTICES_PER_WORD || practices.size() > MAXIMUM_PRACTICES_PER_WORD) {
            reject("Each vocabulary group must contain 3 to 5 practices");
        }
        var contexts = new HashSet<String>();
        for (var practice : practices) {
            validatePractice(group.vocabularyId(), practice);
            directions.add(practice.direction());
            var contextKey = practice.sourceSentence() + "\u0000" + practice.clozeSentence()
                    + "\u0000" + practice.completeSentence();
            if (!contexts.add(contextKey)) {
                reject("Practices for one vocabulary item must use distinct contexts");
            }
        }
    }

    private void validatePractice(String vocabularyId, GeneratedWordPractice practice) {
        if (practice == null
                || !vocabularyId.equals(practice.vocabularyId())
                || practice.direction() == null
                || isBlank(practice.sourceSentence())
                || isBlank(practice.clozeSentence())
                || !practice.clozeSentence().contains("_____")
                || isBlank(practice.completeSentence())
                || isBlank(practice.exactAnswer())
                || practice.acceptedAnswers().stream().anyMatch(this::isBlank)) {
            reject("Generated practice is incomplete or invalid");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void reject(String reason) {
        throw new InvalidWordPracticeBatchException(reason);
    }
}
