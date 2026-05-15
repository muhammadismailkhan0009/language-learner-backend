package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;

import java.util.Optional;

public interface ReadingParagraphClozeRepo {

    ReadingParagraphClozeSession save(ReadingParagraphClozeSession session);

    Optional<ReadingParagraphClozeSession> findLatestByUserId(String userId);

    Optional<ReadingParagraphClozeSession> findByIdAndUserId(String sessionId, String userId);
}
