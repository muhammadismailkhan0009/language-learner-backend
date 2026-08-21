package com.myriadcode.languagelearner.language_content.application.services.writing_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeBilingualContent;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.ports.LLMPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WritingPracticeLlmAdapter implements WritingPracticeLlmApi {

    private final LLMPort llmPort;

    public WritingPracticeLlmAdapter(LLMPort llmPort) {
        this.llmPort = llmPort;
    }

    @Override
    public String selectTopicForWriting(List<WritingPracticeVocabularySeed> vocabulary,
                                        List<String> previousTopics,
                                        LanguageLevel difficultyLevel) {
        if (vocabulary == null || vocabulary.isEmpty()) {
            return null;
        }
        var result = llmPort.selectWritingTopicForTextGeneration(vocabulary, previousTopics, difficultyLevel);
        return result == null ? null : result.topic();
    }

    @Override
    public WritingPracticeBilingualContent generateBilingualContent(String topic,
                                                                    List<WritingPracticeVocabularySeed> vocabulary,
                                                                    LanguageLevel difficultyLevel,
                                                                    List<String> grammarRuleTitles) {
        if (vocabulary == null || vocabulary.isEmpty()) {
            return new WritingPracticeBilingualContent("", "", List.of());
        }
        var result = llmPort.generateWritingBilingualContent(topic, vocabulary, difficultyLevel, grammarRuleTitles);
        if (result == null) {
            return new WritingPracticeBilingualContent("", "", List.of());
        }
        var usedVocabulary = result.usedVocabulary() == null ? List.<String>of() : result.usedVocabulary().stream()
                .filter(surface -> surface != null && !surface.isBlank())
                .map(String::trim)
                .toList();
        return new WritingPracticeBilingualContent(
                result.englishParagraph(),
                result.germanParagraph(),
                usedVocabulary
        );
    }

    @Override
    public List<WritingPracticeSentencePairSeed> splitIntoSentencePairs(String englishParagraph,
                                                                        String germanParagraph) {
        var result = llmPort.splitWritingContentIntoSentencePairs(englishParagraph, germanParagraph);
        if (result == null || result.sentencePairs() == null) {
            return List.of();
        }
        return result.sentencePairs().stream()
                .map(pair -> new WritingPracticeSentencePairSeed(pair.englishSentence(), pair.germanSentence()))
                .toList();
    }
}
