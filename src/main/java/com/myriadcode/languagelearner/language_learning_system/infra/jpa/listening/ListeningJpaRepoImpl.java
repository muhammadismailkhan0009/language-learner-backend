package com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.myriadcode.languagelearner.language_learning_system.domain.listening.model.WordToListenTo;
import com.myriadcode.languagelearner.language_learning_system.domain.listening.repo.ListeningContentRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening.entities.WordToListenToEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening.repos.WordToListenToEntityJpaRepo;

import java.util.List;
import java.util.UUID;

@Repository
public class ListeningJpaRepoImpl implements ListeningContentRepo {

    @Autowired
    private WordToListenToEntityJpaRepo wordToListenToEntityJpaRepo;

    @Override
    public WordToListenTo saveWordToListenTo(WordToListenTo wordToListenTo) {
        var entity = new WordToListenToEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setWord(wordToListenTo.word());
        entity.setUserId(wordToListenTo.userId().id());
        var savedEntity = wordToListenToEntityJpaRepo.save(entity);
        return new WordToListenTo(wordToListenTo.userId(), savedEntity.getWord());
    }

    @Override
    public List<WordToListenTo> getWordsToListenTo(String userId) {
        var entities = wordToListenToEntityJpaRepo.findAllByUserId(userId);
        return entities.parallelStream()
                .map(entity -> new WordToListenTo(wordToListenToUserId(entity), entity.getWord()))
                .toList();
    }

    private com.myriadcode.languagelearner.common.ids.UserId wordToListenToUserId(WordToListenToEntity entity) {
        return new com.myriadcode.languagelearner.common.ids.UserId(entity.getUserId());
    }
}
