package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.GenerateVocabularyClozeSentencesResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentReadingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchRecentWritingTopicsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyClozeSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeSelectionPolicy;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VocabularyClozeGenerationService {

    private static final int RECENT_TOPIC_LIMIT = 1;
    private static final String GENERAL_TOPIC = "General practice";

    private final VocabularyRepo vocabularyRepo;
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final FetchRecentReadingTopicsApi recentReadingTopicsApi;
    private final FetchRecentWritingTopicsApi recentWritingTopicsApi;
    private final VocabularyClozeLlmApi vocabularyClozeLlmApi;
    private final VocabularyClozeSelectionPolicy selectionPolicy = new VocabularyClozeSelectionPolicy();

    public VocabularyClozeGenerationService(VocabularyRepo vocabularyRepo,
                                            FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                            FetchRecentReadingTopicsApi recentReadingTopicsApi,
                                            FetchRecentWritingTopicsApi recentWritingTopicsApi,
                                            VocabularyClozeLlmApi vocabularyClozeLlmApi) {
        this.vocabularyRepo = vocabularyRepo;
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.recentReadingTopicsApi = recentReadingTopicsApi;
        this.recentWritingTopicsApi = recentWritingTopicsApi;
        this.vocabularyClozeLlmApi = vocabularyClozeLlmApi;
    }

    public GenerateVocabularyClozeSentencesResponse generate(String userId) {
        var flashcards = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        if (flashcards.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary flashcards found for user");
        }

        var reversedCards = flashcards.stream()
                .filter(VocabularyFlashcardReviewRecord::isReversed)
                .toList();
        if (reversedCards.isEmpty()) {
            throw new IllegalArgumentException("No reversed vocabulary flashcards found for user");
        }

        var vocabularyIds = reversedCards.stream()
                .map(VocabularyFlashcardReviewRecord::vocabularyId)
                .distinct()
                .toList();
        var vocabById = vocabularyRepo.findByIds(vocabularyIds).stream()
                .filter(vocabulary -> vocabulary.userId() != null && userId.equals(vocabulary.userId().id()))
                .collect(Collectors.toMap(vocabulary -> vocabulary.id().id(), vocabulary -> vocabulary));
        var candidates = buildCandidates(reversedCards, vocabById);
        if (candidates.isEmpty()) {
            return new GenerateVocabularyClozeSentencesResponse(0);
        }

        var selected = selectionPolicy.selectCandidates(
                userId,
                candidates,
                Instant.now().truncatedTo(ChronoUnit.HOURS)
        );
        if (selected.isEmpty()) {
            return new GenerateVocabularyClozeSentencesResponse(0);
        }

        var seeds = selected.stream()
                .map(candidate -> vocabById.get(candidate.vocabularyId()))
                .filter(java.util.Objects::nonNull)
                .map(vocabulary -> new VocabularyClozeGenerationSeed(
                        vocabulary.id().id(),
                        vocabulary.surface(),
                        vocabulary.translation()
                ))
                .toList();
        if (seeds.isEmpty()) {
            return new GenerateVocabularyClozeSentencesResponse(0);
        }

        var topic = determineTopic(userId);
        var generated = vocabularyClozeLlmApi.generateClozeSentences(topic, seeds);
        if (generated.isEmpty()) {
            throw new IllegalArgumentException("No cloze sentences generated");
        }

        // FIXME: This matches generated LLM results back to vocabulary by exact surface text.
        // If we ever introduce normalization here, we must align vocabulary persistence/uniqueness rules too;
        // otherwise cloze generation would use a different identity rule than the rest of the system.
        // Revisit the same identity assumption in reading and writing LLM flows as well.
        var selectedBySurface = seeds.stream()
                .collect(Collectors.toMap(
                        VocabularyClozeGenerationSeed::surface,
                        seed -> vocabById.get(seed.vocabularyId()),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        var generatedByVocabularySurface = generated.stream()
                .filter(result -> result.vocabSource() != null && !result.vocabSource().isBlank())
                .collect(Collectors.toMap(
                        result -> result.vocabSource().trim(),
                        this::toDomainClozeSentence,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        var generatedCount = 0;
        for (var entry : generatedByVocabularySurface.entrySet()) {
            var vocabulary = selectedBySurface.get(entry.getKey());
            if (vocabulary == null) {
                continue;
            }
            var updated = VocabularyDomainService.withClozeSentence(vocabulary, entry.getValue());
            vocabularyRepo.replaceClozeSentence(vocabulary.id().id(), userId, updated);
            generatedCount++;
        }

        return new GenerateVocabularyClozeSentencesResponse(generatedCount);
    }

    private List<VocabularyClozeCandidate> buildCandidates(List<VocabularyFlashcardReviewRecord> reversedCards,
                                                           Map<String, Vocabulary> vocabById) {
        return reversedCards.stream()
                .map(review -> {
                    var vocabulary = vocabById.get(review.vocabularyId());
                    if (vocabulary == null) {
                        return null;
                    }
                    if (vocabulary.clozeSentence() != null) {
                        return null;
                    }
                    var createdAt = vocabulary.createdAt() == null ? Instant.EPOCH : vocabulary.createdAt();
                    return new VocabularyClozeCandidate(
                            review.flashcardId(),
                            review.vocabularyId(),
                            review.fsrsState(),
                            createdAt,
                            review.due(),
                            review.retrievability(),
                            review.lapses(),
                            review.lastReview()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String determineTopic(String userId) {
        var topics = new ArrayList<String>();
        topics.addAll(recentReadingTopicsApi.findRecentTopics(userId, RECENT_TOPIC_LIMIT));
        topics.addAll(recentWritingTopicsApi.findRecentTopics(userId, RECENT_TOPIC_LIMIT));
        var merged = topics.stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        return merged.isEmpty() ? GENERAL_TOPIC : String.join(" | ", merged);
    }

    private VocabularyClozeSentence toDomainClozeSentence(VocabularyClozeSentenceResult result) {
        validate(result);
        return new VocabularyClozeSentence(
                new VocabularyClozeSentence.VocabularyClozeSentenceId(UUID.randomUUID().toString()),
                result.clozeText().trim(),
                result.hint().trim(),
                result.answerText().trim(),
                result.answerWords().stream().map(String::trim).filter(word -> !word.isBlank()).toList(),
                result.answerTranslation().trim()
        );
    }

    private void validate(VocabularyClozeSentenceResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Cloze sentence result is required");
        }
        if (isBlank(result.clozeText()) || isBlank(result.hint()) || isBlank(result.answerText())
                || isBlank(result.answerTranslation())) {
            throw new IllegalArgumentException("Generated cloze sentence is incomplete");
        }
        if (result.answerWords() == null || result.answerWords().isEmpty()) {
            throw new IllegalArgumentException("Generated cloze sentence must contain answer words");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
