package com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myriadcode.languagelearner.language_learning_system.infra.jpa.listening.entities.WordToListenToEntity;

import java.util.List;

@Repository
public interface WordToListenToEntityJpaRepo extends JpaRepository<WordToListenToEntity, String> {

    List<WordToListenToEntity> findAllByUserId(String userId);
}
