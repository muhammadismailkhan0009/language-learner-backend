package com.myriadcode.languagelearner.language_content.application.services.study;

import com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.StudyAnswerEvaluationResult;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

@Service
public class StudyAnswerEvaluationLlmAdapter implements StudyAnswerEvaluationLlmApi {

    private final LLMPort llmPort;

    public StudyAnswerEvaluationLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public StudyAnswerEvaluationResult evaluate(String sentenceWithBlank,
                                                String expectedAnswer,
                                                String answerTranslation,
                                                String hint,
                                                String userAnswer) {
        var result = llmPort.evaluateStudyAnswer(sentenceWithBlank, expectedAnswer, answerTranslation, hint, userAnswer);
        if (result == null) {
            return new StudyAnswerEvaluationResult(0.0, 0.0, 0.0, "Your answer is incorrect. Try again.");
        }
        return new StudyAnswerEvaluationResult(
                result.semanticMatch(),
                result.formAccuracy(),
                result.confidence(),
                result.feedback() == null || result.feedback().isBlank() ? "Your answer was checked." : result.feedback().trim()
        );
    }
}
