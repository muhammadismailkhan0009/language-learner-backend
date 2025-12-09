package com.myriadcode.languagelearner.language_content.domain.repo;

import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;

import java.util.List;

public interface UserStatsRepo {

    List<UserStatsForContent> getUserStatsForContent(List<String> userIds);

    UserStatsForContent save(UserStatsForContent userStatsForContent);
}
