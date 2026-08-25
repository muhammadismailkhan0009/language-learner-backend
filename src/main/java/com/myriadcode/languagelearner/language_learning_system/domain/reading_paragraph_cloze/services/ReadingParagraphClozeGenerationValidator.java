package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.services;

import com.myriadcode.languagelearner.language_content.application.externals.ClozeParagraphGeneration;
import com.myriadcode.languagelearner.common.enums.ClozePracticeKind;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReadingParagraphClozeGenerationValidator {
    public boolean isValid(ClozeParagraphGeneration generated, Set<String> vocabularyIds, Set<String> grammarRuleIds) {
        return validate(generated, vocabularyIds, grammarRuleIds).isEmpty();
    }

    public List<String> validate(ClozeParagraphGeneration generated, Set<String> vocabularyIds, Set<String> grammarRuleIds) {
        var errors = new ArrayList<String>();
        if (generated == null || generated.paragraphs() == null || generated.paragraphs().isEmpty()) {
            return List.of("paragraphs must contain at least one paragraph");
        }
        var tokens = new HashSet<String>();
        for (int paragraphIndex = 0; paragraphIndex < generated.paragraphs().size(); paragraphIndex++) {
            var paragraph = generated.paragraphs().get(paragraphIndex);
            var path = "paragraphs[" + paragraphIndex + "]";
            if (paragraph == null) { errors.add(path + " must not be null"); continue; }
            if (blank(paragraph.scenarioLabel())) errors.add(path + ".scenarioLabel is required");
            if (blank(paragraph.clozeParagraph())) errors.add(path + ".clozeParagraph is required");
            if (paragraph.blanks() == null || paragraph.blanks().isEmpty()) {
                errors.add(path + ".blanks must contain at least one blank");
                continue;
            }
            if (paragraph.blanks().size() > 4) errors.add(path + ".blanks must contain at most 4 blanks");
            for (int blankIndex = 0; blankIndex < paragraph.blanks().size(); blankIndex++) {
                var item = paragraph.blanks().get(blankIndex);
                var blankPath = path + ".blanks[" + blankIndex + "]";
                if (item == null) { errors.add(blankPath + " must not be null"); continue; }
                if (blank(item.blankToken())) errors.add(blankPath + ".blankToken is required");
                if (blank(item.exactAnswer())) errors.add(blankPath + ".exactAnswer is required");
                if (blank(item.answerExplanation())) errors.add(blankPath + ".answerExplanation is required");
                if (item.practiceKind() == null) errors.add(blankPath + ".practiceKind is required");
                if (!blank(item.blankToken())) {
                    if (!tokens.add(item.blankToken())) errors.add(blankPath + ".blankToken must be unique across all paragraphs");
                    if (!blank(paragraph.clozeParagraph()) && occurrences(paragraph.clozeParagraph(), item.blankToken()) != 1)
                        errors.add(blankPath + ".blankToken must occur exactly once in clozeParagraph");
                }
                var kind = item.practiceKind();
                if (kind == null) continue;
                var grammarIds = item.grammarRuleIds() == null ? java.util.List.<String>of() : item.grammarRuleIds();
                if (grammarIds.stream().anyMatch(this::blank)) errors.add(blankPath + ".grammarRuleIds must not contain blank ids");
                if (grammarIds.size() != new HashSet<>(grammarIds).size()) errors.add(blankPath + ".grammarRuleIds must be unique");
                boolean hasVocabulary = item.vocabularyId() != null && !item.vocabularyId().isBlank();
                boolean hasGrammar = !grammarIds.isEmpty();
                if (hasVocabulary && !vocabularyIds.contains(item.vocabularyId())) errors.add(blankPath + ".vocabularyId is not present in the generation context");
                if (grammarIds.stream().anyMatch(id -> !grammarRuleIds.contains(id))) errors.add(blankPath + ".grammarRuleIds contains an id not present in the generation context");
                if (kind == ClozePracticeKind.VOCABULARY_FORM && (!hasVocabulary || hasGrammar)) errors.add(blankPath + " with VOCABULARY_FORM requires vocabularyId and no grammarRuleIds");
                if (kind == ClozePracticeKind.GRAMMAR && (hasVocabulary || !hasGrammar)) errors.add(blankPath + " with GRAMMAR requires grammarRuleIds and no vocabularyId");
                if (kind == ClozePracticeKind.VOCABULARY_AND_GRAMMAR && (!hasVocabulary || !hasGrammar)) errors.add(blankPath + " with VOCABULARY_AND_GRAMMAR requires both vocabularyId and grammarRuleIds");
            }
        }
        return List.copyOf(errors);
    }

    private int occurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) { count++; index += token.length(); }
        return count;
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
