package com.myriadcode.languagelearner.language_learning_system.application.publishers;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.CreateFlashCardEvent;
import com.myriadcode.languagelearner.common.events.EventPublisher;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import org.springframework.stereotype.Service;

@Service
public class VocabularyFlashCardPublisher {

    private final EventPublisher eventPublisher;

    public VocabularyFlashCardPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void createPrivateVocabularyCards(Vocabulary vocabulary) {
        eventPublisher.publish(new CreateFlashCardEvent(
                vocabulary.id().id(),
                vocabulary.userId().id(),
                ContentRefType.VOCABULARY,
                true
        ));
    }
}
