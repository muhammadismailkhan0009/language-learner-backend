package com.myriadcode.languagelearner.flashcards_study.domain.algorithm;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RevisionSession {

    private static final int RECENT_REVIEW_WINDOW = 5;

    private final Map<String, Integer> shownCount = new HashMap<>();
    private final ArrayDeque<String> recentShownCardIds = new ArrayDeque<>();
    private final Set<String> recentShownCardIdSet = new HashSet<>();
    private String lastShownCardId;

    public int shownTimes(String cardId) {
        return shownCount.getOrDefault(cardId, 0);
    }

    public void markShown(String cardId) {
        shownCount.put(cardId, shownTimes(cardId) + 1);
        lastShownCardId = cardId;
        if (recentShownCardIdSet.remove(cardId)) {
            recentShownCardIds.remove(cardId);
        }
        recentShownCardIds.addLast(cardId);
        recentShownCardIdSet.add(cardId);
        while (recentShownCardIds.size() > RECENT_REVIEW_WINDOW) {
            var expired = recentShownCardIds.removeFirst();
            recentShownCardIdSet.remove(expired);
        }
    }

    public String lastShown() {
        return lastShownCardId;
    }

    public boolean wasShownRecently(String cardId) {
        return recentShownCardIdSet.contains(cardId);
    }
}
