package com.myriadcode.languagelearner.language_content.domain.services;

import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanBlitz;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SyllabusPolicy {

    public Optional<LangConfigsAdaptive> decideNext(List<UserStatsForContent> userStats
    ,LocalDateTime now) {
        var lastAssignedAtOpt =
                userStats.stream()
                        .map(UserStatsForContent::syllabusAssignedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo);

        if (lastAssignedAtOpt.isPresent()) {
            LocalDateTime lastAssignedAt = lastAssignedAtOpt.get();
            if (lastAssignedAt.plusHours(24).isAfter(now)) {
                return Optional.empty();
            }
        }

        var completedConfigs = userStats.stream()
                .map(UserStatsForContent::langConfigsAdaptive)
                .toList();

        return
                GermanBlitz.getNextLessonToGenerateContentFor(completedConfigs);
    }
}
