package com.myriadcode.languagelearner.language_content.domain.model;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UserStatsForContent(UserStatsId id, LangConfigsAdaptive langConfigsAdaptive,
                                  LocalDateTime syllabusAssignedAt,
                                  @NotNull UserId userId) {

    public record UserStatsId(String id) {
    }

    public UserStatsForContent(LangConfigsAdaptive langConfigsAdaptive,
                               LocalDateTime syllabusAssignedAt,
                               @NotNull UserId userId) {
        this(null, langConfigsAdaptive, syllabusAssignedAt, userId);
    }
}
