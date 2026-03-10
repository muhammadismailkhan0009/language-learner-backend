package com.myriadcode.languagelearner.language_learning_system.application.event_handlers;

import com.myriadcode.languagelearner.common.events.ClearVocabularyClozeSentenceEvent;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ClearVocabularyClozeSentenceEventHandler {

    private final VocabularyRepo vocabularyRepo;

    public ClearVocabularyClozeSentenceEventHandler(VocabularyRepo vocabularyRepo) {
        this.vocabularyRepo = vocabularyRepo;
    }

    @EventListener
    public void handle(ClearVocabularyClozeSentenceEvent event) {
        vocabularyRepo.findByIdAndUserId(event.getVocabularyId(), event.getUserId())
                .ifPresent(vocabulary -> vocabularyRepo.replaceClozeSentence(
                        vocabulary.id().id(),
                        event.getUserId(),
                        VocabularyDomainService.withClozeSentence(vocabulary, null)
                ));
    }
}
