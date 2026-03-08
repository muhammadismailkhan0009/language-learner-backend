package com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model;

import com.myriadcode.languagelearner.common.ids.UserId;

import java.time.Instant;
import java.util.List;

public record WritingPracticeSession(
        WritingPracticeSessionId id,
        UserId userId,
        String topic,
        String englishParagraph,
        String germanParagraph,
        Instant createdAt,
        String submittedAnswer,
        Instant submittedAt,
        List<WritingSentencePair> sentencePairs,
        List<WritingVocabularyUsage> vocabularyUsages
) {

    public record WritingPracticeSessionId(String id) {
    }
}
