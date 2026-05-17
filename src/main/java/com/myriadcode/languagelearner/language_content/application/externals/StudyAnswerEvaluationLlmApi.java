package com.myriadcode.languagelearner.language_content.application.externals;

public interface StudyAnswerEvaluationLlmApi {

    StudyAnswerEvaluationResult evaluate(String sentenceWithBlank,
                                         String expectedAnswer,
                                         String answerTranslation,
                                         String hint,
                                         String userAnswer);
}
