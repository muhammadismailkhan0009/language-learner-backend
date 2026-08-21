package com.myriadcode.languagelearner.language_content.application.externals;

import java.util.List;

public record WritingPracticeBilingualContent(
        String englishParagraph,
        String germanParagraph,
        List<String> usedVocabulary
) {
}
