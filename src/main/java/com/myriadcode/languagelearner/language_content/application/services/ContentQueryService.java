package com.myriadcode.languagelearner.language_content.application.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.language_content.application.externals.ChunkRecord;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import com.myriadcode.languagelearner.language_content.application.externals.SentenceRecord;
import com.myriadcode.languagelearner.language_content.application.publishers.ContentPublisher;
import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import com.myriadcode.languagelearner.language_content.domain.repo.UserStatsRepo;
import com.myriadcode.languagelearner.language_content.domain.services.SyllabusPolicy;
import com.myriadcode.languagelearner.user_management.application.externals.UserInformationApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
//FIXME: separate the fetch and data changing apis later into separate interfaces and services
@Service
@RequiredArgsConstructor
public class ContentQueryService implements FetchLanguageContentApi {

    private final LanguageContentRepo languageContentRepo;

    private final UserStatsRepo userStatsRepo;

    private final ContentPublisher contentPublisher;

    private final UserInformationApi userInfoApi;

    private final SyllabusPolicy syllabusPolicy = new SyllabusPolicy();


    @Override
    public ChunkRecord getChunkRecord(String chunkId) {
        var chunk = languageContentRepo.getChunk(chunkId);
        return new ChunkRecord(chunk.chunk(), chunk.translation(), chunk.note());
    }

    @Override
    public SentenceRecord getSentenceRecord(String sentenceId) {
        var sentence = languageContentRepo.getSentence(sentenceId);
        return new SentenceRecord(sentence.sentence(), sentence.translation());
    }

    @Override
    public void generateCardsForUser(UserId userId) {

        var now = LocalDateTime.now();

        /*
        1- Vals.io,Vals.useio
        2- Vals.cpu, Vals.useCpu
        3- .value() - code becomes blocking
         */
        var userStats = Vals.io(() -> userStatsRepo.getUserStatsForContent(List.of(userId.id())));
        var nextSyllabusOpt = userStats.mapCpu(stats -> syllabusPolicy.decideNext(stats, now));

        if (nextSyllabusOpt.value().isEmpty()) {
            log.info("Next syllabus is empty for: {}", userId);
            return;
        }

        var nextSyllabus = nextSyllabusOpt.value().get();
        var sentences = Vals.io(() -> languageContentRepo.getSentencesForLangConfig(nextSyllabus));
        if (sentences.value().isEmpty()) {
            log.info("sentences are empty for: {}", userId);
            return;
        }

        Vals.runIo(() ->
                contentPublisher.createSentencesCards(sentences.value(), userId.id(),true));
        Vals.runIo(() ->
                userStatsRepo.save(new UserStatsForContent(nextSyllabus, now, userId)
                )
        );
    }

}
