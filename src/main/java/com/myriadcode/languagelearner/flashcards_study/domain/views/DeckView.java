package com.myriadcode.languagelearner.flashcards_study.domain.views;

import com.myriadcode.languagelearner.common.enums.DeckInfo;

public record DeckView(DeckInfo id, String name, Integer totalCards) {
}
