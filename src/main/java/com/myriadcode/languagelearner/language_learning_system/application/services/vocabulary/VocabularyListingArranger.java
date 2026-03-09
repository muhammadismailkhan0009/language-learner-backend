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

    private VocabularyListingArranger() {
    }

    static List<Vocabulary> arrange(List<Vocabulary> vocabularies, Instant referenceTime) {
        if (vocabularies == null || vocabularies.isEmpty()) {
            return List.of();
        }

        var orderedByCreation = vocabularies.stream()
                .sorted(Comparator.comparing(Vocabulary::createdAt)
                        .thenComparing(vocabulary -> vocabulary.id().id()))
                .toList();

        var arranged = new ArrayList<Vocabulary>(orderedByCreation.size());
        var epochMinute = Math.floorDiv(referenceTime.getEpochSecond(), 60);

        for (int start = 0; start < orderedByCreation.size(); start += GROUP_SIZE) {
            var end = Math.min(start + GROUP_SIZE, orderedByCreation.size());
            var group = new ArrayList<>(orderedByCreation.subList(start, end));
            Collections.shuffle(group, new Random(seedFor(epochMinute, start)));
            arranged.addAll(group);
        }

        return List.copyOf(arranged);
    }

    private static long seedFor(long epochMinute, int groupStartIndex) {
        return epochMinute * 31L + groupStartIndex;
    }
}
