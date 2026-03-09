package com.myriadcode.languagelearner.flashcards_study.application.services;

import com.myriadcode.languagelearner.flashcards_study.domain.models.FlashCardReview;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        stateFor(userId).recordShownCards(cards);
    }

    private UserCooldownState stateFor(String userId) {
        return cooldownByUser.computeIfAbsent(userId, ignored -> new UserCooldownState());
    }

    private static final class UserCooldownState {
        private final ArrayDeque<String> recentCardIds = new ArrayDeque<>();
        private final Set<String> recentCardIdSet = new HashSet<>();

        synchronized List<FlashCardReview> filterEligible(List<FlashCardReview> cards) {
            var cooldownPreferred = cards.stream()
                    .filter(card -> !recentCardIdSet.contains(card.contentId().id()))
                    .toList();
            return prioritizeDirections(cooldownPreferred.isEmpty() ? cards : cooldownPreferred);
        }

        private List<FlashCardReview> prioritizeDirections(List<FlashCardReview> cards) {
            if (cards.isEmpty()) {
                return List.of();
            }

            var prioritized = new ArrayList<FlashCardReview>();
            var cardsByVocabularyId = new HashMap<String, List<FlashCardReview>>();
            for (FlashCardReview card : cards) {
                cardsByVocabularyId.computeIfAbsent(card.contentId().id(), ignored -> new ArrayList<>())
                        .add(card);
            }

            for (Map.Entry<String, List<FlashCardReview>> entry : cardsByVocabularyId.entrySet()) {
                var vocabularyId = entry.getKey();
                var options = entry.getValue();
                var lastShownDirection = lastShownDirectionByVocabularyId.get(vocabularyId);

                if (lastShownDirection == null) {
                    prioritized.addAll(options);
                    continue;
                }

                var oppositeDirectionCards = options.stream()
                        .filter(card -> card.isReversed() != lastShownDirection)
                        .toList();

                if (!oppositeDirectionCards.isEmpty()) {
                    prioritized.addAll(oppositeDirectionCards);
                    continue;
                }

                prioritized.addAll(options);
            }

            return List.copyOf(prioritized);
        }

        synchronized void recordShownVocabularyIds(List<String> cardIds) {
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

        synchronized void recordShownCards(List<FlashCardReview> cards) {
            for (FlashCardReview card : cards) {
                lastShownDirectionByVocabularyId.put(card.contentId().id(), card.isReversed());
            }
            recordShownVocabularyIds(cards.stream()
                    .map(card -> card.contentId().id())
                    .toList());
        }

        private final Map<String, Boolean> lastShownDirectionByVocabularyId = new HashMap<>();
    }
}
