package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VocabularyDetailValidator {
    public List<ValidatedDetail> validate(List<VocabularyExtractionCandidate> pending,
                                          VocabularyDetailSelection selection) {
        var errors = new ArrayList<String>();
        var byId = pending.stream().collect(Collectors.toMap(value -> value.id().id(), Function.identity()));
        var details = selection == null || selection.details() == null
                ? List.<VocabularyDetailSelection.Detail>of() : selection.details();
        if (details.size() != pending.size()) errors.add("One detail is required for every pending candidate");
        var seen = new HashSet<String>();
        var validated = new ArrayList<ValidatedDetail>();
        for (var detail : details) {
            if (detail == null || detail.candidateId() == null || !seen.add(detail.candidateId())) {
                errors.add("Candidate IDs are required and must be unique");
                continue;
            }
            var candidate = byId.get(detail.candidateId());
            if (candidate == null) {
                errors.add("Unknown candidateId: " + detail.candidateId());
                continue;
            }
            if (!candidate.surface().equals(detail.surface())) errors.add("Candidate surface must not change: " + detail.candidateId());
            if (blank(detail.translation())) errors.add("Translation is required: " + detail.candidateId());
            Vocabulary.EntryKind entryKind = parseEntryKind(detail.entryKind(), detail.candidateId(), errors);
            var examples = validateExamples(detail.exampleSentences(), detail.candidateId(), errors);
            if (entryKind != null && !blank(detail.translation())
                    && candidate.surface().equals(detail.surface()) && !examples.isEmpty()) {
                validated.add(new ValidatedDetail(candidate, detail.translation().trim(), entryKind,
                        detail.notes() == null ? "" : detail.notes().trim(), examples));
            }
        }
        if (!errors.isEmpty()) throw new VocabularyExtractionValidationException(errors);
        return List.copyOf(validated);
    }

    private Vocabulary.EntryKind parseEntryKind(String value, String candidateId, List<String> errors) {
        try {
            return Vocabulary.EntryKind.valueOf(value == null ? "" : value.trim());
        } catch (IllegalArgumentException exception) {
            errors.add("Invalid entryKind: " + candidateId);
            return null;
        }
    }

    private List<VocabularyDetailSelection.ExampleSentence> validateExamples(
            List<VocabularyDetailSelection.ExampleSentence> examples, String candidateId, List<String> errors) {
        if (examples == null || examples.isEmpty()) {
            errors.add("At least one example sentence is required: " + candidateId);
            return List.of();
        }
        for (var example : examples) {
            if (example == null || blank(example.sentence()) || blank(example.translation())) {
                errors.add("Example sentence and translation are required: " + candidateId);
                return List.of();
            }
        }
        return List.copyOf(examples);
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public record ValidatedDetail(VocabularyExtractionCandidate candidate, String translation,
                                  Vocabulary.EntryKind entryKind, String notes,
                                  List<VocabularyDetailSelection.ExampleSentence> examples) {}
}
