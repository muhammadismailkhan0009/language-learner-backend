package com.myriadcode.languagelearner.language_learning_system.application.services.writing_practice;

import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeSentencePairSeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.application.externals.WritingPracticeLlmApi;
import com.myriadcode.languagelearner.language_learning_system.domain.writing_practice.model.WritingSentencePair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class WritingPracticeContentAssembler {

    Set<String> findUsedVocabularySurfaces(
            WritingPracticeLlmApi writingPracticeLlmApi,
            List<WritingPracticeVocabularySeed> selectedVocab,
            String englishParagraph,
            String germanParagraph
    ) {
        var usedVocabulary = writingPracticeLlmApi.identifyUsedVocabulary(selectedVocab, englishParagraph, germanParagraph);
        if (usedVocabulary == null || usedVocabulary.isEmpty()) {
            return Set.of();
        }
        return usedVocabulary.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeSurface)
                .filter(surface -> !surface.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    List<WritingSentencePair> buildSentencePairs(List<WritingPracticeSentencePairSeed> pairs,
                                                 String englishParagraph,
                                                 String germanParagraph) {
        if (pairs == null || pairs.isEmpty()) {
            return List.of(new WritingSentencePair(
                    new WritingSentencePair.WritingSentencePairId(UUID.randomUUID().toString()),
                    englishParagraph,
                    germanParagraph,
                    0
            ));
        }
        return java.util.stream.IntStream.range(0, pairs.size())
                .mapToObj(index -> {
                    var pair = pairs.get(index);
                    return new WritingSentencePair(
                            new WritingSentencePair.WritingSentencePairId(UUID.randomUUID().toString()),
                            sanitizeParagraph(pair.englishSentence()),
                            sanitizeParagraph(pair.germanSentence()),
                            index
                    );
                })
                .toList();
    }

    String sanitizeParagraph(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String normalizeSurface(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
