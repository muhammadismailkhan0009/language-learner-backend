package com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_practice.response;

import java.util.List;

public record ReadingPracticeParagraphResponse(
        String paragraphText,
        List<String> sentences
) {
}
