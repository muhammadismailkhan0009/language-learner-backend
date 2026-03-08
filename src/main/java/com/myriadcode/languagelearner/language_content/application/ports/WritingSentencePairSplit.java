package com.myriadcode.languagelearner.language_content.application.ports;

import java.util.List;

public record WritingSentencePairSplit(
        List<SentencePair> sentencePairs
) {
    public record SentencePair(String englishSentence, String germanSentence) {
    }
}
