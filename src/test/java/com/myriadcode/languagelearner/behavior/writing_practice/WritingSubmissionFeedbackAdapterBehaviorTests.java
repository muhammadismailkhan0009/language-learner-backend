package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.ports.WritingSubmissionFeedback;
import com.myriadcode.languagelearner.language_content.application.services.writing_practice.WritingSubmissionFeedbackLlmAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WritingSubmissionFeedbackAdapterBehaviorTests {

    @Test
    @DisplayName("generateFeedback: returns fallback when LLM response is null")
    void generateFeedbackReturnsFallbackWhenNull() {
        var llmPort = mock(LLMPort.class);
        when(llmPort.generateWritingSubmissionFeedback("en", "de-ref", "de-user")).thenReturn(null);

        var adapter = new WritingSubmissionFeedbackLlmAdapter(llmPort);
        var feedback = adapter.generateFeedback("en", "de-ref", "de-user");

        assertThat(feedback).isEqualTo("Submission checked. Keep writing and focus on clear German sentence structure.");
    }

    @Test
    @DisplayName("generateFeedback: trims non-blank LLM feedback")
    void generateFeedbackTrimsNonBlankFeedback() {
        var llmPort = mock(LLMPort.class);
        when(llmPort.generateWritingSubmissionFeedback("en", "de-ref", "de-user"))
                .thenReturn(new WritingSubmissionFeedback("  Good structure, fix verb position in subordinate clauses.  "));

        var adapter = new WritingSubmissionFeedbackLlmAdapter(llmPort);
        var feedback = adapter.generateFeedback("en", "de-ref", "de-user");

        assertThat(feedback).isEqualTo("Good structure, fix verb position in subordinate clauses.");
    }
}
