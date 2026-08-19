package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface FetchRecentWritingVocabularyUsageApi {

    List<Set<String>> findRecentVocabularyUsageSessionSets(String userId, int limit);
}
