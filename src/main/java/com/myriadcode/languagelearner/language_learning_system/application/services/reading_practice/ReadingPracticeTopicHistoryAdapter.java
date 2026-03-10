package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadingPracticeTopicHistoryAdapter implements FetchRecentReadingTopicsApi {

    private final ReadingPracticeRepo readingPracticeRepo;

    public ReadingPracticeTopicHistoryAdapter(ReadingPracticeRepo readingPracticeRepo) {
        this.readingPracticeRepo = readingPracticeRepo;
    }

    @Override
    public List<String> findRecentTopics(String userId, int limit) {
        return readingPracticeRepo.findRecentTopicsByUserId(userId, limit);
    }
}
