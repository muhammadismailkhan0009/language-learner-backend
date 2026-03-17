package com.myriadcode.languagelearner.language_content.application.services.reading_practice;

import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeReadingContent;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReadingPracticeLlmAdapter implements ReadingPracticeLlmApi {

    private final LLMPort llmPort;

    public ReadingPracticeLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public String selectTopicForTextGeneration(List<ReadingPracticeVocabularySeed> vocabulary,
                                               List<String> previousTopics,
                                               String difficultyLevel) {
        if (vocabulary == null || vocabulary.isEmpty()) {
            return null;
        }
        return llmPort.selectReadingTopicForTextGeneration(vocabulary, previousTopics, difficultyLevel).topic();
    }

    @Override
    public ReadingPracticeReadingContent generateReadingContent(String topic,
                                                                List<ReadingPracticeVocabularySeed> vocabulary,
                                                                String difficultyLevel) {
        if (vocabulary == null || vocabulary.isEmpty()) {
            return new ReadingPracticeReadingContent(List.of());
        }
        var content = llmPort.generateReadingContent(topic, vocabulary, difficultyLevel);
        if (content == null || content.paragraphs() == null) {
            return new ReadingPracticeReadingContent(List.of());
        }
        var paragraphs = content.paragraphs().stream()
                .map(paragraph -> new ReadingPracticeReadingContent.Paragraph(
                        paragraph.text(),
                        paragraph.sentences()
                ))
                .toList();
        return new ReadingPracticeReadingContent(paragraphs);
    }

    @Override
    public List<String> identifyUsedVocabulary(List<ReadingPracticeVocabularySeed> vocabulary,
                                               String readingText) {
        if (vocabulary == null || vocabulary.isEmpty()) {
            return List.of();
        }
        var result = llmPort.identifyUsedReadingVocabulary(vocabulary, readingText);
        if (result == null || result.usedSurfaces() == null) {
            return List.of();
        }
        return result.usedSurfaces().stream()
                .filter(surface -> surface != null && !surface.isBlank())
                .map(String::trim)
                .toList();
    }
}
