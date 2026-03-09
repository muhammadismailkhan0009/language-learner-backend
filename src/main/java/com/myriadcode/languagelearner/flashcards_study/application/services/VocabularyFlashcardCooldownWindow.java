package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
class VocabularyFlashcardCooldownWindow {

    private static final int COOLDOWN_CARD_COUNT = 5;

    private final ConcurrentHashMap<String, UserCooldownState> cooldownByUser = new ConcurrentHashMap<>();

    List<FlashCardReview> filterEligible(String userId, List<FlashCardReview> cards) {
        return stateFor(userId).filterEligible(cards);
    }

    void recordShown(String userId, List<FlashCardReview> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        stateFor(userId).recordShown(cards.stream()
                .map(card -> card.id().id())
                .toList());
    }

    private UserCooldownState stateFor(String userId) {
        return cooldownByUser.computeIfAbsent(userId, ignored -> new UserCooldownState());
    }

    private static final class UserCooldownState {
        private final ArrayDeque<String> recentCardIds = new ArrayDeque<>();
        private final Set<String> recentCardIdSet = new HashSet<>();

        synchronized List<FlashCardReview> filterEligible(List<FlashCardReview> cards) {
            return cards.stream()
                    .filter(card -> !recentCardIdSet.contains(card.id().id()))
                    .toList();
        }

        synchronized void recordShown(List<String> cardIds) {
            for (String cardId : cardIds) {
                if (recentCardIdSet.remove(cardId)) {
                    recentCardIds.remove(cardId);
                }

                recentCardIds.addLast(cardId);
                recentCardIdSet.add(cardId);

                while (recentCardIds.size() > COOLDOWN_CARD_COUNT) {
                    var expired = recentCardIds.removeFirst();
                    recentCardIdSet.remove(expired);
                }
            }
        }
    }
}
