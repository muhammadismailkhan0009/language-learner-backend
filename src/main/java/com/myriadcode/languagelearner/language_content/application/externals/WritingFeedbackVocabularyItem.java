package com.myriadcode.languagelearner.language_content.application.externals;

public record WritingFeedbackVocabularyItem(
        String vocabularyId,
        String germanTarget,
        String englishMeaning,
        String partOfSpeech,
        boolean required
) {
}
