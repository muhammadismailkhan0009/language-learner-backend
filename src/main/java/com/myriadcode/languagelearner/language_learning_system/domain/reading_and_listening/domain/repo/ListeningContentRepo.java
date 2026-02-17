package com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.domain.repo;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.domain.listening.WordToListenTo;

import java.util.List;

public interface ListeningContentRepo {

    WordToListenTo saveWordToListenTo(WordToListenTo wordToListenTo);

    List<WordToListenTo> getWordsToListenTo(String userId);
}
