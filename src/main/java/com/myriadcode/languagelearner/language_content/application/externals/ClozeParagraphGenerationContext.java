package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import java.util.List;

public record ClozeParagraphGenerationContext(LanguageLevel learnerLevel,
                                               List<VocabularySource> vocabulary,
                                               List<GrammarSource> grammarRules) {
    public record VocabularySource(String id, String surface, String translation, String entryKind, String notes) {}
    public record GrammarSource(String id, String identifier, String name, String level,
                                List<String> explanationParagraphs, List<Example> examples) {}
    public record Example(String sentence, String translation) {}
}
