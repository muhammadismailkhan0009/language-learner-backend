package com.myriadcode.languagelearner.flashcards_study.application.mappers;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;

import java.util.List;

public final class FsrsRescheduleResultMapper {

    private FsrsRescheduleResultMapper() {
    }

    public static FsrsRescheduleResult toDomain(com.myriadcode.fsrs.api.models.RescheduleResult result) {
        if (result == null) {
            return null;
        }
        return new FsrsRescheduleResult(
                FsrsCardMapper.toDomain(result.card()),
                result.log() == null ? List.of() : List.of(ReviewLogMapper.toDomain(result.log()))
        );
    }

    public static com.myriadcode.fsrs.api.models.RescheduleResult toLibrary(FsrsRescheduleResult result) {
        if (result == null) {
            return null;
        }
        return new com.myriadcode.fsrs.api.models.RescheduleResult(
                FsrsCardMapper.toLibrary(result.card()),
                ReviewLogMapper.toLibrary(result.reviewLogs().isEmpty() ? null : result.reviewLogs().getLast())
        );
    }
}
