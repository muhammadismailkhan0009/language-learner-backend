package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.GeneratedWordPracticeGroup;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects.WordPracticeCandidate;

import java.util.List;

public record WordPracticeGeneration(
        List<WordPracticeCandidate> selected,
        List<GeneratedWordPracticeGroup> groups
) {
    public WordPracticeGeneration {
        selected = selected == null ? List.of() : List.copyOf(selected);
        groups = groups == null ? List.of() : List.copyOf(groups);
    }
}
