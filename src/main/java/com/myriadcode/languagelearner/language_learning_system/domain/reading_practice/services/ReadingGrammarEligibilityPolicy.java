package com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.services;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.LinkedHashSet;
import java.util.List;

public class ReadingGrammarEligibilityPolicy {

    public List<String> selectTitles(LanguageLevel learnerLevel, List<Candidate> candidates) {
        if (learnerLevel == null || candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        var titles = new LinkedHashSet<String>();
        candidates.stream()
                .filter(candidate -> candidate != null && candidate.active())
                .filter(candidate -> candidate.level() != null)
                .filter(candidate -> candidate.level().ordinal() <= learnerLevel.ordinal())
                .map(Candidate::title)
                .filter(title -> title != null && !title.isBlank())
                .map(String::trim)
                .forEach(titles::add);
        return List.copyOf(titles);
    }

    public record Candidate(String title, LanguageLevel level, boolean active) {
    }
}
