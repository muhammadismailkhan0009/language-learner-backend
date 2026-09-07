package com.myriadcode.languagelearner.behavior.writing_practice;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice.WritingPracticeService;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeScenario;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingPracticeSession;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WritingPracticeFreeWritingSubmissionTests {

    private final WritingPracticeRepo repo = mock(WritingPracticeRepo.class);
    private final WritingPracticeService service = new WritingPracticeService(
            repo, mock(), mock(), mock(), null, mock(), mock(), mock(), mock());

    @Test
    void draftAllowsBlankFieldsAndPersistsBoth() {
        when(repo.findByIdAndUserId("session-1", "user-1")).thenReturn(Optional.of(session()));

        service.submitAnswer("user-1", "session-1", "scenario-1", null, "  partial free text  ", true);

        verify(repo).updateSubmission(
                "session-1", "scenario-1", "user-1", "", "partial free text", null, null, null, null);
    }

    @Test
    void finalSubmissionRequiresTranslation() {
        when(repo.findByIdAndUserId("session-1", "user-1")).thenReturn(Optional.of(session()));

        assertThatThrownBy(() -> service.submitAnswer(
                "user-1", "session-1", "scenario-1", " ", "freier Text", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Submitted answer must not be blank");
    }

    @Test
    void finalSubmissionRequiresFreeWriting() {
        when(repo.findByIdAndUserId("session-1", "user-1")).thenReturn(Optional.of(session()));

        assertThatThrownBy(() -> service.submitAnswer(
                "user-1", "session-1", "scenario-1", "Ubersetzung", " ", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Free writing text must not be blank");
    }

    @Test
    void finalSubmissionPersistsBothAndMarksSubmitted() {
        when(repo.findByIdAndUserId("session-1", "user-1")).thenReturn(Optional.of(session()));

        service.submitAnswer(
                "user-1", "session-1", "scenario-1", "  Ubersetzung  ", "  freier Text  ", false);

        verify(repo).updateSubmission(
                org.mockito.ArgumentMatchers.eq("session-1"),
                org.mockito.ArgumentMatchers.eq("scenario-1"),
                org.mockito.ArgumentMatchers.eq("user-1"),
                org.mockito.ArgumentMatchers.eq("Ubersetzung"),
                org.mockito.ArgumentMatchers.eq("freier Text"),
                org.mockito.ArgumentMatchers.any(Instant.class),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull());
    }

    private WritingPracticeSession session() {
        return new WritingPracticeSession(
                new WritingPracticeSession.WritingPracticeSessionId("session-1"),
                new UserId("user-1"),
                Instant.EPOCH,
                List.of(new WritingPracticeScenario(
                        new WritingPracticeScenario.WritingPracticeScenarioId("scenario-1"),
                        0, "Topic", "English", "German", "Write freely.", null,
                        null, null, null, null, null, List.of(), List.of())));
    }
}
