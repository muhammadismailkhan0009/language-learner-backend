package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingPracticeGeneration(List<Scenario> scenarios) {
    public record Scenario(
            String topic,
            String englishParagraph,
            String germanParagraph,
            List<WritingPracticeSentencePairSeed> sentencePairs,
            List<String> usedVocabulary
    ) {
    }
}
