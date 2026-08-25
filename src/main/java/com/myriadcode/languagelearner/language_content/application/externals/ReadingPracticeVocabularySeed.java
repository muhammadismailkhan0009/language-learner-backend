package com.myriadcode.languagelearner.language_content.application.externals;

public record ReadingPracticeVocabularySeed(
        String id,
        String surface,
        String translation
) {
    public ReadingPracticeVocabularySeed(String surface, String translation) { this(null, surface, translation); }
}
