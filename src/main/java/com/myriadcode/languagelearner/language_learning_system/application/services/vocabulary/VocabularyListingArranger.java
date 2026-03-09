package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

final class VocabularyListingArranger {

    private static final int GROUP_SIZE = 8;
    private static final int RANDOMIZED_GROUP_COUNT = 3;

    private VocabularyListingArranger() {
    }

    static List<Vocabulary> arrange(List<Vocabulary> vocabularies, Instant referenceTime) {
        if (vocabularies == null || vocabularies.isEmpty()) {
            return List.of();
        }

        var orderedByCreation = vocabularies.stream()
                .sorted(Comparator.comparing(Vocabulary::createdAt)
                        .thenComparing(vocabulary -> vocabulary.id().id())
                        .reversed())
                .toList();

        var grouped = new ArrayList<List<Vocabulary>>();
        var epochMinute = Math.floorDiv(referenceTime.getEpochSecond(), 60);

        for (int start = 0; start < orderedByCreation.size(); start += GROUP_SIZE) {
            var end = Math.min(start + GROUP_SIZE, orderedByCreation.size());
            grouped.add(List.copyOf(orderedByCreation.subList(start, end)));
        }

        var randomizedGroupCount = Math.min(RANDOMIZED_GROUP_COUNT, grouped.size());
        var firstGroups = new ArrayList<>(grouped.subList(0, randomizedGroupCount));
        Collections.shuffle(firstGroups, new Random(seedFor(epochMinute)));

        var arranged = new ArrayList<Vocabulary>(orderedByCreation.size());
        for (List<Vocabulary> group : firstGroups) {
            arranged.addAll(group);
        }
        for (int index = randomizedGroupCount; index < grouped.size(); index++) {
            arranged.addAll(grouped.get(index));
        }

        return List.copyOf(arranged);
    }

    private static long seedFor(long epochMinute) {
        return epochMinute * 31L + RANDOMIZED_GROUP_COUNT;
    }
}
