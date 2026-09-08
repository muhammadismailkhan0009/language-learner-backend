package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WordPracticeAnswerService {
    private final WordPracticeRepo practiceRepo;

    public WordPracticeAnswerService(WordPracticeRepo practiceRepo) {
        this.practiceRepo = practiceRepo;
    }

    @Transactional
    public boolean submit(String userId, String practiceId, String answer) {
        requireNonblank(userId, "userId");
        requireNonblank(practiceId, "practiceId");
        var practice = practiceRepo.findByIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Word Practice not found for user"));
        boolean correct = practice.isCorrect(answer);
        if (correct) {
            practiceRepo.deleteByIdAndUserId(practiceId, userId);
        }
        return correct;
    }

    private void requireNonblank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
