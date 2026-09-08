package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.aggregates.WordPractice;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WordPracticeQueryService {
    private final WordPracticeRepo practiceRepo;

    public WordPracticeQueryService(WordPracticeRepo practiceRepo) {
        this.practiceRepo = practiceRepo;
    }

    @Transactional(readOnly = true)
    public List<WordPractice> findAll(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return practiceRepo.findByUserId(userId);
    }
}
