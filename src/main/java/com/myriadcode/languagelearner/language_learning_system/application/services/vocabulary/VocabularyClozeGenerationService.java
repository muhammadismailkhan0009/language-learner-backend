package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeGenerationSeed;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeLlmApi;
import com.myriadcode.languagelearner.language_content.application.externals.VocabularyClozeSentenceResult;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VocabularyClozeGenerationService {

    private static final int RECENT_READING_TOPIC_LIMIT = 3;
    private static final int RECENT_WRITING_TOPIC_LIMIT = 3;
    private static final int MAX_TOPIC_CONTEXT = 5;
    private static final String GENERAL_TOPIC = "General practice";
    private static final Pattern CLOZE_BLANK_PATTERN = Pattern.compile("_{3,}");

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
        List<VocabularyClozeSentenceResult> generated;
        try (var ignored = LlmUserContextHolder.scoped(userId)) {
            generated = vocabularyClozeLlmApi.generateClozeSentences(topic, seeds);
        }
        if (generated.isEmpty()) {
            throw new IllegalArgumentException("No cloze sentences generated");
        }
        log.info("Cloze generation LLM raw output userId={} topic='{}' seeds={} generated={}",
                userId, topic, seeds.size(), generated.size());
        for (int index = 0; index < generated.size(); index++) {
            var row = generated.get(index);
            log.info("Cloze generation LLM row[{}] userId={} row={}", index, userId, row);
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

        var filteredOutRows = new ArrayList<FilteredOutClozeRow>();
        var rowsToPersist = new ArrayList<PersistableClozeRow>();
        Set<String> queuedVocabularyIds = new HashSet<>();
        for (var result : generated) {
            if (result == null || isBlank(result.vocabSource())) {
                filteredOutRows.add(new FilteredOutClozeRow("missing_vocab_source", null, result));
                continue;
            }
            var vocabulary = selectedBySurface.get(result.vocabSource().trim());
            if (vocabulary == null) {
                filteredOutRows.add(new FilteredOutClozeRow("source_not_matched_to_selected_seed",
                        result.vocabSource().trim(), result));
                continue;
            }

            var latest = vocabularyRepo.findByIdAndUserId(vocabulary.id().id(), userId).orElse(null);
            if (latest == null || latest.clozeSentence() != null) {
                filteredOutRows.add(new FilteredOutClozeRow("vocabulary_missing_or_already_has_cloze",
                        vocabulary.id().id(), result));
                continue;
            }
            VocabularyClozeSentence sentence;
            try {
                sentence = toDomainClozeSentence(result);
            } catch (RuntimeException exception) {
                filteredOutRows.add(new FilteredOutClozeRow(
                        "invalid_generated_row_" + exception.getMessage(),
                        vocabulary.id().id(),
                        result
                ));
                continue;
            }
            if (!queuedVocabularyIds.add(vocabulary.id().id())) {
                filteredOutRows.add(new FilteredOutClozeRow("duplicate_generated_row_for_same_vocabulary",
                        vocabulary.id().id(), result));
                continue;
            }

            rowsToPersist.add(new PersistableClozeRow(
                    vocabulary.id().id(),
                    latest,
                    sentence,
                    result
            ));
        }

        if (!filteredOutRows.isEmpty()) {
            log.info("Cloze generation filtered out rows userId={} count={}", userId, filteredOutRows.size());
            for (int index = 0; index < filteredOutRows.size(); index++) {
                var row = filteredOutRows.get(index);
                log.info("Cloze generation filtered row[{}] userId={} reason={} key={} raw={}",
                        index, userId, row.reason(), row.key(), row.raw());
            }
        } else {
            log.info("Cloze generation filtered out rows userId={} count=0", userId);
        }

        log.info("Cloze generation rows ready for persistence userId={} count={}", userId, rowsToPersist.size());
        for (int index = 0; index < rowsToPersist.size(); index++) {
            var row = rowsToPersist.get(index);
            log.info("Cloze generation persist row[{}] userId={} vocabularyId={} vocabSource={} cloze='{}'",
                    index, userId, row.vocabularyId(), row.raw().vocabSource(), row.sentence().clozeText());
        }

        var generatedCount = 0;
        for (var row : rowsToPersist) {
            var updated = VocabularyDomainService.withClozeSentence(row.latestVocabulary(), row.sentence());
            vocabularyRepo.replaceClozeSentence(row.vocabularyId(), userId, updated);
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
        var readingTopics = recentReadingTopicsApi.findRecentTopics(userId, RECENT_READING_TOPIC_LIMIT);
        if (readingTopics != null) {
            topics.addAll(readingTopics);
        }
        var writingTopics = recentWritingTopicsApi.findRecentTopics(userId, RECENT_WRITING_TOPIC_LIMIT);
        if (writingTopics != null) {
            topics.addAll(writingTopics);
        }
        var merged = topics.stream()
                .filter(topic -> topic != null && !topic.isBlank())
                .map(String::trim)
                .distinct()
                .limit(MAX_TOPIC_CONTEXT)
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
        var normalizedWords = result.answerWords().stream()
                .map(word -> word == null ? "" : word.trim())
                .filter(word -> !word.isBlank())
                .toList();
        if (normalizedWords.isEmpty()) {
            throw new IllegalArgumentException("Generated cloze sentence answer words must be non-blank");
        }
        if (countBlanks(result.clozeText()) != normalizedWords.size()) {
            throw new IllegalArgumentException("Generated cloze sentence blank count must match answer words");
        }
    }

    private int countBlanks(String clozeText) {
        if (clozeText == null || clozeText.isBlank()) {
            return 0;
        }
        int count = 0;
        var matcher = CLOZE_BLANK_PATTERN.matcher(clozeText);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private record FilteredOutClozeRow(
            String reason,
            String key,
            VocabularyClozeSentenceResult raw
    ) {
    }

    private record PersistableClozeRow(
            String vocabularyId,
            Vocabulary latestVocabulary,
            VocabularyClozeSentence sentence,
            VocabularyClozeSentenceResult raw
    ) {
    }
}
