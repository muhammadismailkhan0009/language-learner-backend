package com.myriadcode.languagelearner.language_learning_system.reading_and_listening.domain.repo;

import java.util.List;

import com.myriadcode.languagelearner.language_learning_system.reading_and_listening.domain.listening.WordToListenTo;

public interface ListeningContentRepo {

    WordToListenTo saveWordToListenTo(WordToListenTo wordToListenTo);

    List<WordToListenTo> getWordsToListenTo(String userId);
}
