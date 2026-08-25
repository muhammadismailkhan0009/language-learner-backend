package com.myriadcode.languagelearner.language_content.application.services.reading_practice;

import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import java.util.*;

public final class ReadingPracticeReadingContentValidator {
    public List<String> validate(int expectedCount, List<ReadingPracticeVocabularySeed> vocabulary,
                                 ReadingPracticeReadingContent content) {
        var errors = new ArrayList<String>();
        var scenarios = content == null || content.scenarios() == null ? List.<ReadingPracticeReadingContent.Scenario>of() : content.scenarios();
        if (scenarios.size() != expectedCount) errors.add("Expected %d scenarios but received %d".formatted(expectedCount, scenarios.size()));
        var byId = new HashMap<String, ReadingPracticeVocabularySeed>();
        var bySurface = new HashMap<String, ReadingPracticeVocabularySeed>();
        if (vocabulary != null) vocabulary.stream().filter(Objects::nonNull).forEach(seed -> {
            if (!blank(seed.id())) byId.put(seed.id(), seed);
            if (!blank(seed.surface())) bySurface.putIfAbsent(normalize(seed.surface()), seed);
        });
        var labels = new HashSet<String>();
        for (int i = 0; i < scenarios.size(); i++) {
            var scenario = scenarios.get(i);
            if (scenario == null || blank(scenario.scenarioLabel())) errors.add("Scenario %d label is blank".formatted(i));
            else if (!labels.add(scenario.scenarioLabel().trim().toLowerCase(Locale.ROOT))) errors.add("Scenario %d label is duplicated".formatted(i));
            var paragraphs = scenario == null || scenario.paragraphs() == null ? List.<ReadingPracticeReadingContent.Paragraph>of() : scenario.paragraphs();
            if (paragraphs.isEmpty() || paragraphs.size() > 2) errors.add("Scenario %d must contain one or two paragraphs".formatted(i));
            for (int p = 0; p < paragraphs.size(); p++) {
                var paragraph = paragraphs.get(p);
                if (paragraph == null || blank(paragraph.text())) errors.add("Scenario %d paragraph %d text is blank".formatted(i, p));
                var sentences = paragraph == null || paragraph.sentences() == null ? List.<String>of() : paragraph.sentences();
                if (sentences.isEmpty()) errors.add("Scenario %d paragraph %d has no sentences".formatted(i, p));
                else if (sentences.stream().anyMatch(ReadingPracticeReadingContentValidator::blank)) errors.add("Scenario %d paragraph %d contains a blank sentence".formatted(i, p));
            }
            var seen = new HashSet<String>();
            var used = scenario == null || scenario.usedVocabulary() == null
                    ? List.<ReadingPracticeReadingContent.UsedVocabulary>of() : scenario.usedVocabulary();
            for (var reference : used) {
                var seed = reference == null ? null : byId.get(reference.vocabularyId());
                if (seed == null && reference != null && !blank(reference.surface())) {
                    seed = bySurface.get(normalize(reference.surface()));
                }
                if (seed == null) errors.add("Scenario %d contains unknown vocabulary reference: %s / %s"
                        .formatted(i, reference == null ? null : reference.vocabularyId(), reference == null ? null : reference.surface()));
                else if (!seen.add(seed.id())) errors.add("Scenario %d contains duplicate vocabulary reference: %s".formatted(i, seed.id()));
            }
        }
        return List.copyOf(errors);
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
