package com.myriadcode.languagelearner.language_learning_system.application.services.exercise_vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingVocabularyUsageApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingVocabularyUsageApi;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecentExerciseVocabularyUsageService {

    static final int SESSION_LIMIT = 10;

    private final FetchRecentReadingVocabularyUsageApi readingUsageApi;
    private final FetchRecentWritingVocabularyUsageApi writingUsageApi;

    public RecentExerciseVocabularyUsageService(FetchRecentReadingVocabularyUsageApi readingUsageApi,
                                                FetchRecentWritingVocabularyUsageApi writingUsageApi) {
        this.readingUsageApi = readingUsageApi;
        this.writingUsageApi = writingUsageApi;
    }

    public Map<String, Integer> countRecentSessionUsage(String userId) {
        var counts = new HashMap<String, Integer>();
        countSessions(readingUsageApi.findRecentVocabularyUsageSessionSets(userId, SESSION_LIMIT), counts);
        countSessions(writingUsageApi.findRecentVocabularyUsageSessionSets(userId, SESSION_LIMIT), counts);
        return Map.copyOf(counts);
    }

    private void countSessions(List<Set<String>> sessions, Map<String, Integer> counts) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        sessions.stream()
                .filter(java.util.Objects::nonNull)
                .map(Set::copyOf)
                .flatMap(Set::stream)
                .filter(vocabularyId -> vocabularyId != null && !vocabularyId.isBlank())
                .forEach(vocabularyId -> counts.merge(vocabularyId, 1, Integer::sum));
    }
}
