package com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.ReadingParagraphClozeSession;

import java.util.Optional;
import java.util.List;

public interface ReadingParagraphClozeRepo {

    ReadingParagraphClozeSession save(ReadingParagraphClozeSession session);

    List<ReadingParagraphClozeSession> findAllByUserId(String userId);

    Optional<ReadingParagraphClozeSession> findByIdAndUserId(String sessionId, String userId);

    boolean deleteByIdAndUserId(String sessionId, String userId);
}
