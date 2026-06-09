package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingMeaningAnalysisResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingVocabularyEvaluationResult;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WritingFeedbackOutputValidator {

    private static final Set<String> VOCAB_STATUSES = Set.of(
            "correct", "partially_correct", "missing", "wrong", "used_english", "no_evidence"
    );
    private static final Set<String> MEMORY_SIGNALS = Set.of(
            "production_good", "production_hard", "production_again", "no_update"
    );

    public void validateMeaning(WritingMeaningAnalysisResult result) {
        if (result == null || blank(result.overallCoverage())) {
            throw new IllegalArgumentException("Meaning analysis is incomplete");
        }
    }

    public void validateVocabulary(WritingVocabularyEvaluationResult result) {
        if (result == null || result.items() == null) {
            throw new IllegalArgumentException("Vocabulary evaluation is incomplete");
        }
        for (var item : result.items()) {
            if (item == null || blank(item.vocabularyId())) {
                throw new IllegalArgumentException("Vocabulary evaluation item is missing vocabulary id");
            }
            if (!VOCAB_STATUSES.contains(normalize(item.status()))) {
                throw new IllegalArgumentException("Unsupported vocabulary status: " + item.status());
            }
            if (!MEMORY_SIGNALS.contains(normalize(item.memorySignal()))) {
                throw new IllegalArgumentException("Unsupported vocabulary memory signal: " + item.memorySignal());
            }
        }
    }

    public void validateGrammar(WritingGrammarIssueDetectionResult result) {
        if (result == null || result.issues() == null) {
            throw new IllegalArgumentException("Grammar issue detection is incomplete");
        }
    }

    public void validateFeedback(WritingStructuredFeedbackResult result) {
        if (result == null
                || blank(result.overall())
                || blank(result.correctedParagraph())
                || result.topFixes() == null
                || result.vocabulary() == null
                || result.sentenceCorrections() == null
                || result.microPractice() == null
                || blank(result.nextFocus())) {
            throw new IllegalArgumentException("Structured writing feedback is incomplete");
        }
        if (result.topFixes().size() > 3) {
            throw new IllegalArgumentException("Structured writing feedback contains more than 3 top fixes");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
