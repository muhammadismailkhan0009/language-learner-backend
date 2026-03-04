package com.myriadcode.languagelearner.language_content.application.services.reading_practice;

import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadingPracticeLlmAdapter implements ReadingPracticeLlmApi {

    private final LLMPort llmPort;

    public ReadingPracticeLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public List<String> generateTopicCandidates(List<ReadingPracticeVocabularySeed> vocabulary,
                                                String difficultyLevel) {
        return llmPort.generateReadingTopicCandidates(vocabulary, difficultyLevel).topics();
    }

    @Override
    public String generateReadingText(String topic,
                                      List<ReadingPracticeVocabularySeed> vocabulary,
                                      String difficultyLevel) {
        return llmPort.generateReadingContent(topic, vocabulary, difficultyLevel).readingText();
    }
}
