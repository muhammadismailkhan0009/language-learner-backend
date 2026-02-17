package com.myriadcode.languagelearner.language_learning_system.domain.listening.repo;

import java.util.List;

import com.myriadcode.languagelearner.language_learning_system.domain.listening.model.WordToListenTo;

public interface ListeningContentRepo {

    WordToListenTo saveWordToListenTo(WordToListenTo wordToListenTo);

    List<WordToListenTo> getWordsToListenTo(String userId);
}
