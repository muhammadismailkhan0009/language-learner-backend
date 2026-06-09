package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackPipelineLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingFeedbackVocabularyItem;
import com.myriadcode.languagelearner.language_content.application.externals.WritingGrammarIssueDetectionResult;
import com.myriadcode.languagelearner.language_content.application.externals.WritingStructuredFeedbackResult;
import com.myriadcode.languagelearner.language_learning_system.application.services.grammar_rules.GrammarFeedbackOrchestrationService;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingGrammarIssueAnalytics;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingStructuredFeedback;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class WritingFeedbackPipelineService {

    private final WritingFeedbackPipelineLlmApi llmApi;
    private final GrammarFeedbackOrchestrationService grammarFeedbackOrchestrationService;
    private final WritingFeedbackOutputValidator validator;
    private final WritingFeedbackIssueSelector issueSelector;
    private final WritingPracticeRepo writingPracticeRepo;

    public WritingFeedbackPipelineService(WritingFeedbackPipelineLlmApi llmApi,
                                          GrammarFeedbackOrchestrationService grammarFeedbackOrchestrationService,
                                          WritingFeedbackOutputValidator validator,
                                          WritingFeedbackIssueSelector issueSelector,
                                          WritingPracticeRepo writingPracticeRepo) {
        this.llmApi = llmApi;
        this.grammarFeedbackOrchestrationService = grammarFeedbackOrchestrationService;
        this.validator = validator;
        this.issueSelector = issueSelector;
        this.writingPracticeRepo = writingPracticeRepo;
    }

    public WritingFeedbackPipelineResult generateFeedback(WritingPracticeSession session,
                                                          String learnerLevel,
                                                          String learnerGermanAnswer,
                                                          List<WritingFeedbackVocabularyItem> selectedVocabulary) {
        return generateFeedback(session, learnerLevel, learnerGermanAnswer, selectedVocabulary, false);
    }

    public WritingFeedbackPipelineResult generateFeedback(WritingPracticeSession session,
                                                          String learnerLevel,
                                                          String learnerGermanAnswer,
                                                          List<WritingFeedbackVocabularyItem> selectedVocabulary,
                                                          boolean replaceExistingAnalytics) {
        var grammarCatalog = grammarFeedbackOrchestrationService == null
                ? List.<com.myriadcode.languagelearner.language_content.application.externals.GrammarRuleCatalogItem>of()
                : grammarFeedbackOrchestrationService.buildCatalog();

        var meaning = callWithSingleRetry(
                () -> llmApi.analyzeMeaning(learnerLevel, session.englishParagraph(), session.germanParagraph(), learnerGermanAnswer),
                validator::validateMeaning
        );
        var vocabulary = callWithSingleRetry(
                () -> llmApi.evaluateVocabulary(learnerLevel, session.englishParagraph(), session.germanParagraph(), learnerGermanAnswer, selectedVocabulary, meaning),
                validator::validateVocabulary
        );
        var grammarIssues = callWithSingleRetry(
                () -> llmApi.detectGrammarIssues(learnerLevel, session.englishParagraph(), session.germanParagraph(), learnerGermanAnswer, grammarCatalog, meaning, vocabulary),
                validator::validateGrammar
        );

        var topIssues = issueSelector.selectTopIssues(grammarIssues);

        var feedback = callWithSingleRetry(
                () -> llmApi.composeFeedback(learnerLevel, session.englishParagraph(), session.germanParagraph(), learnerGermanAnswer, meaning, vocabulary, grammarIssues, topIssues),
                validator::validateFeedback
        );

        if (replaceExistingAnalytics) {
            writingPracticeRepo.deleteGrammarIssueAnalytics(session.id().id(), session.userId().id());
        }
        writingPracticeRepo.saveGrammarIssueAnalytics(toAnalytics(session, grammarIssues));

        return new WritingFeedbackPipelineResult(toDomainFeedback(feedback), toFeedbackText(feedback));
    }

    private <T> T callWithSingleRetry(Supplier<T> call, Consumer<T> validation) {
        RuntimeException failure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                var result = call.get();
                validation.accept(result);
                return result;
            } catch (RuntimeException exception) {
                failure = exception;
            }
        }
        throw failure;
    }

    private List<WritingGrammarIssueAnalytics> toAnalytics(WritingPracticeSession session,
                                                           WritingGrammarIssueDetectionResult result) {
        if (result == null || result.issues() == null || result.issues().isEmpty()) {
            return List.of();
        }
        var now = Instant.now();
        return result.issues().stream()
                .map(issue -> new WritingGrammarIssueAnalytics(
                        new WritingGrammarIssueAnalytics.WritingGrammarIssueAnalyticsId(UUID.randomUUID().toString()),
                        session.id(),
                        session.userId(),
                        emptyToNull(issue.grammarRuleIdentifier()),
                        blankDefault(issue.issueType(), "writing_issue"),
                        issue.priority(),
                        issue.learnerText(),
                        issue.correctedText(),
                        issue.shortExplanation(),
                        Math.max(1, issue.occurrenceCount()),
                        now
                ))
                .toList();
    }

    private WritingStructuredFeedback toDomainFeedback(WritingStructuredFeedbackResult result) {
        return new WritingStructuredFeedback(
                result.overall(),
                result.correctedParagraph(),
                result.topFixes().stream()
                        .map(fix -> new WritingStructuredFeedback.TopFix(fix.title(), fix.learnerText(), fix.correctedText(), fix.explanation()))
                        .toList(),
                new WritingStructuredFeedback.VocabularySummary(
                        safe(result.vocabulary().good()),
                        safe(result.vocabulary().needsPractice())
                ),
                result.sentenceCorrections().stream()
                        .map(correction -> new WritingStructuredFeedback.SentenceCorrection(correction.learnerSentence(), correction.correctedSentence(), correction.explanation()))
                        .toList(),
                result.microPractice().stream()
                        .map(item -> new WritingStructuredFeedback.MicroPracticeItem(item.prompt(), item.expectedAnswer()))
                        .toList(),
                result.nextFocus()
        );
    }

    private String toFeedbackText(WritingStructuredFeedbackResult feedback) {
        return "Overall: %s\n\nCorrected paragraph:\n%s\n\nNext focus: %s"
                .formatted(feedback.overall(), feedback.correctedParagraph(), feedback.nextFocus());
    }

    private List<String> safe(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record WritingFeedbackPipelineResult(WritingStructuredFeedback structuredFeedback, String feedbackText) {
    }
}
