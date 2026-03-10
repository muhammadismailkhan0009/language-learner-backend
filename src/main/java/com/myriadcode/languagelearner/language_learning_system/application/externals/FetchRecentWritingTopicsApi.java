package com.myriadcode.languagelearner.language_learning_system.application.externals;

import java.util.List;

public interface FetchRecentWritingTopicsApi {

    List<String> findRecentTopics(String userId, int limit);
}
