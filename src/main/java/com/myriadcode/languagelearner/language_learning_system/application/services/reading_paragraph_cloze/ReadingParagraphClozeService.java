package com.myriadcode.languagelearner.language_learning_system.application.services.reading_paragraph_cloze;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeVocabularySeed;
import com.myriadcode.languagelearner.language_content.infra.llm.LlmUserContextHolder;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.reading_paragraph_cloze.response.ReadingParagraphClozeSessionResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.*;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.model.*;
import com.myriadcode.languagelearner.language_learning_system.domain.reading_paragraph_cloze.repo.ReadingParagraphClozeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyClozeSelectionPolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadingParagraphClozeService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 300;
    private static final String DIFFICULTY_LEVEL = "B1";
    private static final int RECENT_READING_TOPIC_LIMIT = 3;
    private static final int RECENT_WRITING_TOPIC_LIMIT = 3;
    private static final int MAX_TOPIC_CONTEXT = 5;
    private static final String GENERAL_TOPIC = "General practice";
    private static final int MIN_SENTENCES_PER_PARAGRAPH = 5;
    private static final int MAX_SENTENCES_PER_PARAGRAPH = 7;
    private static final int MAX_CLOZE_ITEMS_PER_PARAGRAPH = 4;

    private final ReadingParagraphClozeRepo repo;
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final VocabularyRepo vocabularyRepo;
    private final FetchRecentReadingTopicsApi recentReadingTopicsApi;
    private final FetchRecentWritingTopicsApi recentWritingTopicsApi;
    private final com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi readingPracticeLlmApi;
    private final ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi;
    private final VocabularyClozeSelectionPolicy selectionPolicy = new VocabularyClozeSelectionPolicy();

    public ReadingParagraphClozeService(ReadingParagraphClozeRepo repo,
                                        FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                        VocabularyRepo vocabularyRepo,
                                        FetchRecentReadingTopicsApi recentReadingTopicsApi,
                                        FetchRecentWritingTopicsApi recentWritingTopicsApi,
                                        com.myriadcode.languagelearner.language_content.application.externals.ReadingPracticeLlmApi readingPracticeLlmApi,
                                        ReviewVocabularyFlashcardApi reviewVocabularyFlashcardApi) {
        this.repo = repo;
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.vocabularyRepo = vocabularyRepo;
        this.recentReadingTopicsApi = recentReadingTopicsApi;
        this.recentWritingTopicsApi = recentWritingTopicsApi;
        this.readingPracticeLlmApi = readingPracticeLlmApi;
        this.reviewVocabularyFlashcardApi = reviewVocabularyFlashcardApi;
    }

    public ReadingParagraphClozeSessionResponse createSession(String userId, Integer requestedLimit) {
        var existing = repo.findLatestByUserId(userId).orElse(null);
        if (existing != null) {
            var existingResponse = toResponse(existing, userId);
            if (!"COMPLETED".equals(existingResponse.status())) {
                return existingResponse;
            }
        }

        int limit = normalizeLimit(requestedLimit);

        var flashcards = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        var reversedCards = flashcards.stream().filter(VocabularyFlashcardReviewRecord::isReversed).toList();
        if (reversedCards.isEmpty()) {
            throw new IllegalArgumentException("No reversed vocabulary flashcards found for user");
        }

        var vocabularyIds = reversedCards.stream().map(VocabularyFlashcardReviewRecord::vocabularyId).distinct().toList();
        var vocabById = vocabularyRepo.findByIds(vocabularyIds).stream()
                .filter(v -> v.userId() != null && userId.equals(v.userId().id()))
                .collect(Collectors.toMap(v -> v.id().id(), v -> v));

        var candidates = buildCandidates(reversedCards, vocabById);
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary candidates found for reading paragraph cloze");
        }

        var selected = selectionPolicy.selectCandidates(userId, candidates, Instant.now().truncatedTo(ChronoUnit.HOURS));
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("Unable to select vocabulary candidates for reading paragraph cloze");
        }
        if (selected.size() > limit) {
            selected = selected.subList(0, limit);
        }

        var selectedVocab = selected.stream()
                .map(candidate -> vocabById.get(candidate.vocabularyId()))
                .filter(Objects::nonNull)
                .toList();
        if (selectedVocab.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary found for selected candidates");
        }

        var seeds = selectedVocab.stream()
                .map(v -> new ReadingPracticeVocabularySeed(v.surface(), v.translation()))
                .toList();

        var topic = GENERAL_TOPIC;
        var clozeGenerated = generateValidatedCloze(topic, seeds, selected, vocabById, userId);
        var paragraphMappings = mapParagraphsFromLlm(clozeGenerated, selected, vocabById);
        if (paragraphMappings.isEmpty()) {
            throw new IllegalArgumentException("Unable to map cloze paragraph vocabulary references");
        }
        var rootParagraph = paragraphMappings.getFirst().paragraph();
        var clozeParagraph = rootParagraph.clozeParagraph();
        var clozeCards = paragraphMappings.stream()
                .flatMap(mapping -> mapping.cards().stream())
                .toList();
        var paragraphs = paragraphMappings.stream().map(ParagraphMapping::paragraph).toList();

        var session = new ReadingParagraphClozeSession(
                new ReadingParagraphClozeSession.ReadingParagraphClozeSessionId(UUID.randomUUID().toString()),
                new UserId(userId),
                topic,
                clozeParagraph,
                Instant.now(),
                paragraphs,
                clozeCards
        );

        return toResponse(repo.save(session), userId);
    }

    public ReadingParagraphClozeSessionResponse getActiveSession(String userId) {
        var session = repo.findLatestByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No active reading paragraph cloze session found"));
        var response = toResponse(session, userId);
        if ("COMPLETED".equals(response.status())) {
            throw new IllegalArgumentException("No active reading paragraph cloze session found");
        }
        return response;
    }

    public ReadingParagraphClozeSessionResponse rateCard(String sessionId, String userId, String flashcardId, Rating rating) {
        var session = repo.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Reading paragraph cloze session not found"));
        boolean hasCard = session.cards().stream().anyMatch(card -> flashcardId.equals(card.flashcardId()));
        if (!hasCard) {
            throw new IllegalArgumentException("Flashcard does not belong to this reading paragraph cloze session");
        }

        // Source-of-truth review path: update the actual vocabulary flashcard state (FSRS/log/event behavior)
        // exactly like existing reverse vocabulary card review flows.
        reviewVocabularyFlashcardApi.reviewVocabularyFlashcard(flashcardId, rating);
        if (rating == Rating.GOOD || rating == Rating.EASY) {
            var remainingCards = session.cards().stream()
                    .filter(card -> !flashcardId.equals(card.flashcardId()))
                    .toList();
            var remainingByParagraph = session.paragraphs().stream()
                    .map(paragraph -> new ReadingParagraphClozeParagraph(
                            paragraph.id(),
                            paragraph.paragraphIndex(),
                            paragraph.scenarioLabel(),
                            paragraph.clozeParagraph(),
                            paragraph.createdAt(),
                            paragraph.cards().stream().filter(card -> !flashcardId.equals(card.flashcardId())).toList()
                    ))
                    .filter(paragraph -> !paragraph.cards().isEmpty())
                    .toList();
            var nextParagraph = remainingByParagraph.isEmpty() ? null : remainingByParagraph.getFirst();
            var updated = new ReadingParagraphClozeSession(
                    session.id(),
                    session.userId(),
                    session.topic(),
                    nextParagraph == null ? session.clozeParagraph() : nextParagraph.clozeParagraph(),
                    session.createdAt(),
                    remainingByParagraph,
                    remainingCards
            );
            var saved = repo.save(updated);
            return toResponse(saved == null ? updated : saved, userId);
        }
        return toResponse(session, userId);
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
                .filter(Objects::nonNull)
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

    private List<ParagraphMapping> mapParagraphsFromLlm(
            com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration generated,
            List<VocabularyClozeCandidate> selected,
            Map<String, Vocabulary> vocabById) {
        if (generated.paragraphs() == null || generated.paragraphs().isEmpty()) return List.of();

        var candidateBySurface = selected.stream()
                .filter(candidate -> vocabById.containsKey(candidate.vocabularyId()))
                .collect(Collectors.toMap(
                        candidate -> normalizeSurface(vocabById.get(candidate.vocabularyId()).surface()),
                        candidate -> candidate,
                        (first, ignored) -> first
                ));

        var result = new ArrayList<ParagraphMapping>();
        var queuedFlashcards = new HashSet<String>();
        int paragraphIndex = 0;
        for (var paragraph : generated.paragraphs()) {
            if (paragraph == null || paragraph.items() == null || paragraph.items().isEmpty()) continue;
            var paragraphId = UUID.randomUUID().toString();
            var paragraphCards = new ArrayList<ReadingParagraphClozeCard>();
            for (var item : paragraph.items()) {
                if (item == null || item.vocabSource() == null || item.vocabSource().isBlank()) continue;
                var candidate = candidateBySurface.get(normalizeSurface(item.vocabSource()));
                if (candidate == null) continue;
                if (!queuedFlashcards.add(candidate.flashcardId())) continue;
                paragraphCards.add(new ReadingParagraphClozeCard(
                        new ReadingParagraphClozeCard.ReadingParagraphClozeCardId(UUID.randomUUID().toString()),
                        paragraphId,
                        candidate.flashcardId(),
                        candidate.vocabularyId(),
                        Instant.now()
                ));
            }
            if (paragraphCards.isEmpty()) continue;
            result.add(new ParagraphMapping(
                    new ReadingParagraphClozeParagraph(
                            new ReadingParagraphClozeParagraph.ReadingParagraphClozeParagraphId(paragraphId),
                            paragraphIndex++,
                            paragraph.scenarioLabel() == null || paragraph.scenarioLabel().isBlank()
                                    ? "Scenario " + (paragraphIndex)
                                    : paragraph.scenarioLabel().trim(),
                            paragraph.clozeParagraph().trim(),
                            Instant.now(),
                            paragraphCards
                    ),
                    paragraphCards
            ));
        }
        return result;
    }

    private List<String> toAnswerWords(String surface) {
        return Arrays.stream(surface.trim().split("\\s+"))
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String buildBlankToken(String surface) {
        var count = Math.max(1, toAnswerWords(surface).size());
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "___")
                .collect(Collectors.joining(" "));
    }

    private int normalizeLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        if (requestedLimit < 1) {
            return 1;
        }
        return Math.min(requestedLimit, MAX_LIMIT);
    }

    private ReadingParagraphClozeSessionResponse toResponse(ReadingParagraphClozeSession session, String userId) {
        int ratedCount = computeRatedCountFromSourceOfTruth(session, userId);
        int totalCount = session.totalCount();
        String status = ratedCount >= totalCount ? "COMPLETED" : "ACTIVE";

        var activeParagraph = session.paragraphs() == null || session.paragraphs().isEmpty()
                ? null
                : session.paragraphs().stream()
                .filter(paragraph -> paragraph.cards() != null && !paragraph.cards().isEmpty())
                .findFirst()
                .orElse(session.paragraphs().getFirst());

        var activeCards = activeParagraph == null ? session.cards() : activeParagraph.cards();
        var vocabularyIds = activeCards.stream()
                .map(ReadingParagraphClozeCard::vocabularyId)
                .distinct()
                .toList();
        var vocabById = vocabularyRepo.findByIds(vocabularyIds).stream()
                .filter(v -> v.userId() != null && userId.equals(v.userId().id()))
                .collect(Collectors.toMap(v -> v.id().id(), v -> v));

        var cards = activeCards.stream()
                .map(card -> {
                    var vocab = vocabById.get(card.vocabularyId());
                    var surface = vocab == null ? "" : vocab.surface();
                    var translation = vocab == null ? "" : vocab.translation();
                    return new ReadingParagraphClozeSessionResponse.Card(
                            card.id().id(),
                            card.flashcardId(),
                            card.vocabularyId(),
                            surface,
                            translation,
                            buildBlankToken(surface),
                            toAnswerWords(surface)
                    );
                })
                .toList();
        return new ReadingParagraphClozeSessionResponse(
                session.id().id(),
                activeParagraph == null ? session.topic() : activeParagraph.scenarioLabel(),
                activeParagraph == null ? session.clozeParagraph() : activeParagraph.clozeParagraph(),
                status,
                ratedCount,
                totalCount,
                cards,
                session.createdAt()
        );
    }

    private int computeRatedCountFromSourceOfTruth(ReadingParagraphClozeSession session, String userId) {
        var reviewsByCardId = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .collect(Collectors.toMap(VocabularyFlashcardReviewRecord::flashcardId, value -> value, (first, ignored) -> first));
        int count = 0;
        for (var card : session.cards()) {
            var review = reviewsByCardId.get(card.flashcardId());
            if (review != null && review.lastReview() != null && !review.lastReview().isBefore(session.createdAt())) {
                count++;
            }
        }
        return count;
    }

    private String normalizeSurface(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration generateValidatedCloze(
            String topic,
            List<ReadingPracticeVocabularySeed> seeds,
            List<VocabularyClozeCandidate> selected,
            Map<String, Vocabulary> vocabById,
            String userId) {
        var first = generateCloze(topic, seeds, userId);
        if (isValid(first, selected, vocabById)) return first;
        var retry = generateCloze(topic + " | Retry with strict constraints", seeds, userId);
        if (!isValid(retry, selected, vocabById)) {
            throw new IllegalArgumentException("Unable to generate valid reading paragraph cloze content");
        }
        return retry;
    }

    private com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration generateCloze(
            String topic, List<ReadingPracticeVocabularySeed> seeds, String userId) {
        try (var ignored = LlmUserContextHolder.scoped(userId)) {
            return readingPracticeLlmApi.generateReadingParagraphCloze(topic, seeds, DIFFICULTY_LEVEL);
        }
    }

    private boolean isValid(
            com.myriadcode.languagelearner.language_content.application.externals.ReadingParagraphClozeGeneration generated,
            List<VocabularyClozeCandidate> selected,
            Map<String, Vocabulary> vocabById) {
        if (generated == null || generated.paragraphs() == null || generated.paragraphs().isEmpty()) return false;
        for (var paragraph : generated.paragraphs()) {
            if (paragraph == null || paragraph.clozeParagraph() == null || paragraph.clozeParagraph().isBlank()) return false;
            int sentenceCount = countSentences(paragraph.clozeParagraph());
            if (sentenceCount < MIN_SENTENCES_PER_PARAGRAPH || sentenceCount > MAX_SENTENCES_PER_PARAGRAPH) return false;
            if (paragraph.items() == null || paragraph.items().isEmpty() || paragraph.items().size() > MAX_CLOZE_ITEMS_PER_PARAGRAPH) return false;
            for (var item : paragraph.items()) {
                if (item == null || item.vocabSource() == null || item.vocabSource().isBlank()) return false;
            }
        }
        return true;
    }

    private int countSentences(String paragraph) {
        var trimmed = paragraph == null ? "" : paragraph.trim();
        if (trimmed.isEmpty()) return 0;
        return (int) Arrays.stream(trimmed.split("(?<=[.!?])\\s+"))
                .filter(value -> !value.isBlank())
                .count();
    }

    private record ParagraphMapping(
            ReadingParagraphClozeParagraph paragraph,
            List<ReadingParagraphClozeCard> cards
    ) {}
}
