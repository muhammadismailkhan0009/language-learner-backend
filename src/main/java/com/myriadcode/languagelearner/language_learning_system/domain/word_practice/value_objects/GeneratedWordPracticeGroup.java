package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.value_objects;

import java.util.List;

public record GeneratedWordPracticeGroup(String vocabularyId, List<GeneratedWordPractice> practices) {
    public GeneratedWordPracticeGroup {
        practices = practices == null ? List.of() : List.copyOf(practices);
    }
}
