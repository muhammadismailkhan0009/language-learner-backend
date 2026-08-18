package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;

import java.util.List;

public record WritingGenerationContext(LanguageLevel learnerLevel, List<String> grammarRuleTitles) {
}
