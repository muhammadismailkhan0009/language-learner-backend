package com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.infra.jpa.repos;

import com.myriadcode.languagelearner.language_learning_system.domain.reading_and_listening.infra.jpa.entities.WordToListenToEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordToListenToEntityJpaRepo extends JpaRepository<WordToListenToEntity, String> {

    List<WordToListenToEntity> findAllByUserId(String userId);
}
