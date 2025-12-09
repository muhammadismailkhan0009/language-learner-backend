package com.myriadcode.languagelearner.language_content.application.publishers;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.EventPublisher;
import com.myriadcode.languagelearner.language_content.domain.events.CreateCardEvent;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentPublisher {


    private final EventPublisher eventPublisher;

    public void createSentencesCards(List<Sentence> sentences, String userId, boolean isReversed) {
        sentences.parallelStream().forEach(sentence ->
                eventPublisher.publish(
                        new CreateCardEvent(sentence.id().id(), userId, ContentRefType.SENTENCE, isReversed)
                )
        );
    }
}
