package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.services;

import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeBlank.PracticeKind;
import java.util.HashSet;
import java.util.Set;

public class ReadingParagraphClozeGenerationValidator {
    public boolean isValid(ClozeParagraphGeneration generated, Set<String> vocabularyIds, Set<String> grammarRuleIds) {
        if (generated == null || generated.paragraphs() == null || generated.paragraphs().isEmpty()) return false;
        var tokens = new HashSet<String>();
        for (var paragraph : generated.paragraphs()) {
            if (paragraph == null || blank(paragraph.scenarioLabel()) || blank(paragraph.clozeParagraph())
                    || paragraph.blanks() == null || paragraph.blanks().isEmpty() || paragraph.blanks().size() > 4) return false;
            for (var item : paragraph.blanks()) {
                if (item == null || blank(item.blankToken()) || blank(item.exactAnswer()) || blank(item.answerExplanation()) || blank(item.practiceKind())) return false;
                if (!tokens.add(item.blankToken()) || occurrences(paragraph.clozeParagraph(), item.blankToken()) != 1) return false;
                PracticeKind kind;
                try { kind = PracticeKind.valueOf(item.practiceKind().trim()); }
                catch (IllegalArgumentException exception) { return false; }
                var grammarIds = item.grammarRuleIds() == null ? java.util.List.<String>of() : item.grammarRuleIds();
                if (grammarIds.stream().anyMatch(this::blank) || grammarIds.size() != new HashSet<>(grammarIds).size()) return false;
                boolean hasVocabulary = item.vocabularyId() != null && !item.vocabularyId().isBlank();
                boolean hasGrammar = !grammarIds.isEmpty();
                if (hasVocabulary && !vocabularyIds.contains(item.vocabularyId())) return false;
                if (grammarIds.stream().anyMatch(id -> !grammarRuleIds.contains(id))) return false;
                if (kind == PracticeKind.VOCABULARY_FORM && (!hasVocabulary || hasGrammar)) return false;
                if (kind == PracticeKind.GRAMMAR && (hasVocabulary || !hasGrammar)) return false;
                if (kind == PracticeKind.VOCABULARY_AND_GRAMMAR && (!hasVocabulary || !hasGrammar)) return false;
            }
        }
        return true;
    }

    private int occurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) { count++; index += token.length(); }
        return count;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
