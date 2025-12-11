package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import java.util.HashMap;
import java.util.Map;

public class RevisionSession {

    private final Map<String, Integer> shownCount = new HashMap<>();
    private String lastShownCardId;

    public int shownTimes(String cardId) {
        return shownCount.getOrDefault(cardId, 0);
    }

    public void markShown(String cardId) {
        shownCount.put(cardId, shownTimes(cardId) + 1);
        lastShownCardId = cardId;
    }

    public String lastShown() {
        return lastShownCardId;
    }
}
