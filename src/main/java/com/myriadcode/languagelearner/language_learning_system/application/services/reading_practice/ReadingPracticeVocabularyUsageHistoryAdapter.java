package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingVocabularyUsageApi;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_practice.repo.ReadingPracticeRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ReadingPracticeVocabularyUsageHistoryAdapter implements FetchRecentReadingVocabularyUsageApi {

    private final ReadingPracticeRepo readingPracticeRepo;

    public ReadingPracticeVocabularyUsageHistoryAdapter(ReadingPracticeRepo readingPracticeRepo) {
        this.readingPracticeRepo = readingPracticeRepo;
    }

    @Override
    public List<Set<String>> findRecentVocabularyUsageSessionSets(String userId, int limit) {
        return readingPracticeRepo.findRecentVocabularyUsageSessionSetsByUserId(userId, limit);
    }
}
