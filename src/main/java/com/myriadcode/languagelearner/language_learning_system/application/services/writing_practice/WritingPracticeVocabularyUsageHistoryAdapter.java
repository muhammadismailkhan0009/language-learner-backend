package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingVocabularyUsageApi;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.repo.WritingPracticeRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WritingPracticeVocabularyUsageHistoryAdapter implements FetchRecentWritingVocabularyUsageApi {

    private final WritingPracticeRepo writingPracticeRepo;

    public WritingPracticeVocabularyUsageHistoryAdapter(WritingPracticeRepo writingPracticeRepo) {
        this.writingPracticeRepo = writingPracticeRepo;
    }

    @Override
    public List<Set<String>> findRecentVocabularyUsageSessionSets(String userId, int limit) {
        return writingPracticeRepo.findRecentVocabularyUsageSessionSetsByUserId(userId, limit);
    }
}
