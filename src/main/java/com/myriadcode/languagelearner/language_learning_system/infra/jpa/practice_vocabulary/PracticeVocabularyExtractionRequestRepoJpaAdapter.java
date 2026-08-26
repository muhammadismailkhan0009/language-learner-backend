package com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.model.PracticeVocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.practice_vocabulary.repo.PracticeVocabularyExtractionRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.entities.PracticeVocabularyExtractionRequestEntity;
import com.myriadcode.languagelearner.language_learning_system.infra.jpa.practice_vocabulary.repos.PracticeVocabularyExtractionRequestJpaRepo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class PracticeVocabularyExtractionRequestRepoJpaAdapter implements PracticeVocabularyExtractionRequestRepo {
    private final PracticeVocabularyExtractionRequestJpaRepo jpaRepo;

    public PracticeVocabularyExtractionRequestRepoJpaAdapter(PracticeVocabularyExtractionRequestJpaRepo jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public PracticeVocabularyExtractionRequest save(PracticeVocabularyExtractionRequest request) {
        var saved = jpaRepo.save(new PracticeVocabularyExtractionRequestEntity(
                request.userId().id(), request.text(), request.createdAt()));
        return new PracticeVocabularyExtractionRequest(
                new UserId(saved.getUserId()), saved.getText(), saved.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PracticeVocabularyExtractionRequest> findByUserId(String userId) {
        return jpaRepo.findById(userId).map(value -> new PracticeVocabularyExtractionRequest(
                new UserId(value.getUserId()), value.getText(), value.getCreatedAt()));
    }

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        jpaRepo.deleteById(userId);
    }
}
