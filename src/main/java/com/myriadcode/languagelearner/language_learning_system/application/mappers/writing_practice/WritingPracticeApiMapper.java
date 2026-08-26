package com.myriadcode.languagelearner.language_learning_system.application.mappers.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeSessionSummaryResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingPracticeScenarioResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingSentencePairResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.writing_practice.response.WritingVocabularyFlashCardView;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingStructuredFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface WritingPracticeApiMapper {

    WritingPracticeApiMapper INSTANCE = Mappers.getMapper(WritingPracticeApiMapper.class);

    default WritingPracticeSessionResponse toResponse(
            WritingPracticeSession session,
            java.util.function.Function<java.util.List<com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingVocabularyUsage>, java.util.List<WritingVocabularyFlashCardView>> flashcards
    ) {
        return new WritingPracticeSessionResponse(session.id().id(), session.scenarios().stream()
                .map(scenario -> toScenario(scenario, flashcards.apply(scenario.vocabularyUsages())))
                .toList(), session.createdAt());
    }

    default WritingPracticeSessionSummaryResponse toSummary(WritingPracticeSession session) {
        var first = session.scenarios().isEmpty() ? null : session.scenarios().getFirst();
        return new WritingPracticeSessionSummaryResponse(session.id().id(), first == null ? "" : first.topic(),
                session.createdAt(), first == null ? "" : preview(first.englishParagraph()),
                session.scenarios().stream().mapToInt(value -> value.vocabularyUsages().size()).sum(),
                !session.scenarios().isEmpty() && session.scenarios().stream().allMatch(value -> value.submittedAt() != null));
    }

    default WritingPracticeScenarioResponse toScenario(WritingPracticeScenario scenario,
                                                        java.util.List<WritingVocabularyFlashCardView> flashcards) {
        return new WritingPracticeScenarioResponse(
                scenario.id().id(), scenario.position(), scenario.topic(), scenario.englishParagraph(), scenario.germanParagraph(),
                scenario.submittedAnswer(), scenario.submittedAt(), scenario.feedbackText(), toFeedback(scenario.structuredFeedback()),
                scenario.feedbackGeneratedAt(), scenario.sentencePairs().stream().map(this::toSentencePairResponse).toList(), flashcards);
    }

    default WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse toFeedback(WritingStructuredFeedback value) {
        if (value == null) return null;
        return new WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse(value.overall(), value.correctedParagraph(),
                value.topFixes().stream().map(fix -> new WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse.TopFix(fix.title(), fix.learnerText(), fix.correctedText(), fix.explanation())).toList(),
                new WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse.VocabularySummary(value.vocabulary().good(), value.vocabulary().needsPractice()),
                value.sentenceCorrections().stream().map(item -> new WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse.SentenceCorrection(item.learnerSentence(), item.correctedSentence(), item.explanation())).toList(),
                value.microPractice().stream().map(item -> new WritingPracticeScenarioResponse.WritingStructuredFeedbackResponse.MicroPracticeItem(item.prompt(), item.expectedAnswer())).toList(), value.nextFocus());
    }

    WritingSentencePairResponse toSentencePairResponse(
            com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair pair);

    @Named("preview")
    default String preview(String englishParagraph) {
        if (englishParagraph == null) {
            return "";
        }
        var limit = Math.min(englishParagraph.length(), 180);
        return englishParagraph.substring(0, limit);
    }

    @Named("usageCount")
    default int usageCount(java.util.List<?> usages) {
        return usages == null ? 0 : usages.size();
    }

    @Named("isSubmitted")
    default boolean isSubmitted(java.time.Instant submittedAt) {
        return submittedAt != null;
    }
}
