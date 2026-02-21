package com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.response;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;
import java.util.List;

public record PublicVocabularyResponse(
        String publicVocabularyId,
        String sourceVocabularyId,
        String publishedByUserId,
        Instant publishedAt,
        Vocabulary.EntryKind entryKind,
        String surface,
        String translation,
        String notes,
        List<ExampleSentenceResponse> exampleSentences
) {

    public record ExampleSentenceResponse(String id, String sentence, String translation) {
    }
}
