package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WritingPracticeTopicHistoryAdapter implements FetchRecentWritingTopicsApi {

    private final WritingPracticeRepo writingPracticeRepo;

    public WritingPracticeTopicHistoryAdapter(WritingPracticeRepo writingPracticeRepo) {
        this.writingPracticeRepo = writingPracticeRepo;
    }

    @Override
    public List<String> findRecentTopics(String userId, int limit) {
        return writingPracticeRepo.findRecentTopicsByUserId(userId, limit);
    }
}
