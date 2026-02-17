package com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.application.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.domain.listening.WordToListenTo;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.domain.repo.ListeningContentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListeningStepOrchestrationService {

    private final ListeningContentRepo listeningContentRepo;

    public WordToListenTo saveWordToListenTo(String userId, WordToListenTo wordToListenTo) {
        return listeningContentRepo.saveWordToListenTo(
                new WordToListenTo(new UserId(userId), wordToListenTo.word()));
    }

    public List<WordToListenTo> fetchWordsToListenTo(String userId) {
        return listeningContentRepo.getWordsToListenTo(userId);
    }
}
