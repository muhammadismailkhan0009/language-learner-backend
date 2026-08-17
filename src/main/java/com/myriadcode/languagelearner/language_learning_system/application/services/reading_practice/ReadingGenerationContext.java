package com.myriadcode.languagelearner.language_learning_system.application.services.reading_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.List;

public record ReadingGenerationContext(LanguageLevel learnerLevel, List<String> grammarRuleTitles) {
}
