package com.myriadcode.languagelearner.flashcards_study.application.mappers;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FsrsRescheduleResult;

public final class FsrsRescheduleResultMapper {

    private FsrsRescheduleResultMapper() {
    }

    public static FsrsRescheduleResult toDomain(com.myriadcode.fsrs.api.models.RescheduleResult result) {
        if (result == null) {
            return null;
        }
        return new FsrsRescheduleResult(
                FsrsCardMapper.toDomain(result.card()),
                ReviewLogMapper.toDomain(result.log())
        );
    }

    public static com.myriadcode.fsrs.api.models.RescheduleResult toLibrary(FsrsRescheduleResult result) {
        if (result == null) {
            return null;
        }
        return new com.myriadcode.fsrs.api.models.RescheduleResult(
                FsrsCardMapper.toLibrary(result.card()),
                ReviewLogMapper.toLibrary(result.log())
        );
    }
}
