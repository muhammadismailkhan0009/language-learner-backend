package com.myriadcode.languagelearner.language_content.application.services;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.concurnas_like_library.Vals;
import com.myriadcode.languagelearner.language_content.application.controllers.sentences.response.SentenceDataResponse;
import com.myriadcode.languagelearner.language_content.application.externals.ChunkRecord;
import com.myriadcode.languagelearner.language_content.application.externals.FetchLanguageContentApi;
import com.myriadcode.languagelearner.language_content.application.externals.SentenceRecord;
import com.myriadcode.languagelearner.language_content.application.publishers.ContentPublisher;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.UserStatsForContent;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanAdaptive;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanBlitz;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import com.myriadcode.languagelearner.language_content.domain.repo.UserStatsRepo;
import com.myriadcode.languagelearner.language_content.domain.services.SyllabusPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
//FIXME: separate the fetch and data changing apis later into separate interfaces and services
@Service
@RequiredArgsConstructor
public class ContentQueryService implements FetchLanguageContentApi {

    private final LanguageContentRepo languageContentRepo;

    private final UserStatsRepo userStatsRepo;

    private final ContentPublisher contentPublisher;

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

    public List<SentenceDataResponse> fetchAllSentences(){
        var sentences = languageContentRepo.getAllSentences();

        // Create a map for quick lookup: (scenario, function) -> List<Sentence>
        Map<String, List<Sentence>> sentenceMap = sentences.stream()
                .collect(Collectors.groupingBy(s -> {
                    var scenario = s.langConfigsAdaptive().scenario();
                    var function = s.langConfigsAdaptive().function();
                    return scenario.name() + "|" + function.name();
                }));

        // Build response following GermanBlitz order
        List<SentenceDataResponse> result = new ArrayList<>();
        GermanAdaptive.ScenarioEnum currentScenario = null;
        List<SentenceDataResponse.SentenceFunction> currentFunctions = new ArrayList<>();

        for (GermanBlitz lesson : GermanBlitz.values()) {
            var scenario = lesson.getScenario();
            var function = lesson.getFunction();
            String key = scenario.name() + "|" + function.name();

            // If scenario changed, save previous scenario and start new one
            if (currentScenario != null && !currentScenario.equals(scenario)) {
                if (!currentFunctions.isEmpty()) {
                    result.add(new SentenceDataResponse(currentScenario, new ArrayList<>(currentFunctions)));
                }
                currentFunctions.clear();
            }

            // Check if we have sentences for this (scenario, function) combination
            List<Sentence> sentencesForLesson = sentenceMap.get(key);
            if (sentencesForLesson != null && !sentencesForLesson.isEmpty()) {
                // Check if we already added this function for current scenario
                boolean functionExists = currentFunctions.stream()
                        .anyMatch(f -> f.function().equals(function));

                if (!functionExists) {
                    var sentenceContents = sentencesForLesson.stream()
                            .map(s -> new SentenceDataResponse.SentenceContent(
                                    s.data().sentence(),
                                    s.data().translation()))
                            .toList();

                    currentFunctions.add(new SentenceDataResponse.SentenceFunction(function, sentenceContents));
                }
            }

            currentScenario = scenario;
        }

        // Add the last scenario if it has functions
        if (currentScenario != null && !currentFunctions.isEmpty()) {
            result.add(new SentenceDataResponse(currentScenario, currentFunctions));
        }

        return result;
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
