package com.myriadcode.languagelearner.language_content.application.schedulers;

import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.domain.model.Sentence;
import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.GermanBlitz;
import com.myriadcode.languagelearner.language_content.domain.repo.LanguageContentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LLMSchedulerService {

    @Autowired
    private LLMPort llmPort;

    @Autowired
    private LanguageContentRepo languageContentRepo;


    //    TODO: later when we add chunks and vocabs, we can query generated sentences and simply add them separately.
//    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void generateSentences() {

        var blitzLessonsAlreadyGenerated = languageContentRepo.getBlitzLessonsForWhichSentencesAreGenerated();

        var languageConfigs = GermanBlitz.getNextLessonToGenerateContentFor(blitzLessonsAlreadyGenerated);
        if (languageConfigs.isPresent()) {

            var sentenceData = llmPort.generateSentences(languageConfigs.get());
            var sentences = sentenceData.parallelStream().map(
                            sentence ->
                                    new Sentence(
                                            new Sentence.SentenceId(UUID.randomUUID().toString()),
                                            sentence, languageConfigs.get()))
                    .toList();
            languageContentRepo.saveSentences(sentences);
            System.out.println("Generated sentences for sentences " + sentences.size());
        }
    }

//    TODO: generate chunks and vocab separately for the generated sentences. need to check which sentences don't have them
//    TODO: or, generate chunks together with sentence later once add exercise.


}
