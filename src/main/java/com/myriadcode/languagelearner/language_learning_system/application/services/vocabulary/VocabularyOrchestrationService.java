package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary.VocabularyApiMapper;
import com.myriadcode.languagelearner.language_learning_system.application.publishers.VocabularyFlashCardPublisher;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import com.myriadcode.fsrs.api.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VocabularyOrchestrationService {

    private static final VocabularyApiMapper VOCABULARY_API_MAPPER = VocabularyApiMapper.INSTANCE;
    private static final int DEFAULT_SONG_SELECTION_LIMIT = 50;
    private static final int MAX_SONG_SELECTION_LIMIT = 300;
    private static final int SONG_RATIO_TOTAL = 10;
    private static final int SONG_NEW_RATIO = 5;
    private static final int SONG_LEARNING_RATIO = 2;
    private static final int SONG_RELEARNING_RATIO = 3;
    private final VocabularyRepo vocabularyRepo;
    private final VocabularyFlashCardPublisher vocabularyFlashCardPublisher;
    private final FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi;
    private final Clock clock;

    @Autowired
    public VocabularyOrchestrationService(VocabularyRepo vocabularyRepo,
                                          VocabularyFlashCardPublisher vocabularyFlashCardPublisher,
                                          FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi) {
        this(vocabularyRepo, vocabularyFlashCardPublisher, vocabularyFlashcardReviewsApi, Clock.systemUTC());
    }

    public VocabularyOrchestrationService(VocabularyRepo vocabularyRepo,
                                          VocabularyFlashCardPublisher vocabularyFlashCardPublisher,
                                          FetchVocabularyFlashcardReviewsApi vocabularyFlashcardReviewsApi,
                                          Clock clock) {
        this.vocabularyRepo = vocabularyRepo;
        this.vocabularyFlashCardPublisher = vocabularyFlashCardPublisher;
        this.vocabularyFlashcardReviewsApi = vocabularyFlashcardReviewsApi;
        this.clock = clock;
    }

    // Backward-compatible constructor for unit tests with fake repos.
    public VocabularyOrchestrationService(VocabularyRepo vocabularyRepo) {
        this(
                vocabularyRepo,
                new VocabularyFlashCardPublisher(domainEvent -> {
                }),
                userId -> List.of(),
                Clock.systemUTC()
        );
    }

    public VocabularyResponse addVocabulary(String userId, AddVocabularyRequest request) {
        if (Vocabulary.EntryKind.WORD == request.entryKind()) {
            var existing = vocabularyRepo.findByUserId(userId).stream()
                    .filter(vocabulary -> vocabulary.entryKind() == Vocabulary.EntryKind.WORD)
                    .filter(vocabulary -> vocabulary.surface().equals(request.surface()))
                    .findFirst();
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Vocabulary already exists for this user");
            }
        }

        var toSave = VocabularyDomainService.create(
                new UserId(userId),
                request.surface(),
                request.translation(),
                request.entryKind(),
                request.notes(),
                VOCABULARY_API_MAPPER.toCreateSentences(request.exampleSentences())
        );
        var saved = vocabularyRepo.save(toSave);
        vocabularyFlashCardPublisher.createPrivateVocabularyCards(saved);
        return VOCABULARY_API_MAPPER.toResponse(saved);
    }

    public VocabularyResponse updateVocabulary(String userId, String vocabularyId, UpdateVocabularyRequest request) {
        var existing = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        var toSave = VocabularyDomainService.edit(
                existing,
                request.surface(),
                request.translation(),
                request.entryKind(),
                request.notes(),
                VOCABULARY_API_MAPPER.toUpdateSentences(request.exampleSentences())
        );
        var saved = vocabularyRepo.save(toSave);
        vocabularyFlashCardPublisher.createPrivateVocabularyCards(saved);
        return VOCABULARY_API_MAPPER.toResponse(saved);
    }

    public List<VocabularyResponse> fetchVocabularies(String userId) {
        var vocabularies = vocabularyRepo.findByUserId(userId);
        var flashcardReviews = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(userId);
        return VocabularyListingArranger.arrange(vocabularies, flashcardReviews, clock.instant()).stream()
                .map(VOCABULARY_API_MAPPER::toResponse)
                .toList();
    }

    public VocabularyResponse fetchVocabulary(String userId, String vocabularyId) {
        var vocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        return VOCABULARY_API_MAPPER.toResponse(vocabulary);
    }

    public List<VocabularyResponse> fetchSongVocabularies(String userId, Integer requestedLimit) {
        int limit = normalizeSongLimit(requestedLimit);
        var flashcardReviews = vocabularyFlashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(review -> review != null && review.isReversed())
                .toList();
        if (flashcardReviews.isEmpty()) {
            return List.of();
        }

        var vocabById = vocabularyRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(vocabulary -> vocabulary.id().id(), Function.identity()));

        var candidates = flashcardReviews.stream()
                .map(review -> toSongCandidate(review, vocabById.get(review.vocabularyId())))
                .filter(java.util.Objects::nonNull)
                .sorted(songCandidateComparator())
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }

        var byState = candidates.stream()
                .collect(Collectors.groupingBy(
                        SongCandidate::state,
                        java.util.LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));

        int targetNew = (limit * SONG_NEW_RATIO) / SONG_RATIO_TOTAL;
        int targetLearning = (limit * SONG_LEARNING_RATIO) / SONG_RATIO_TOTAL;
        int targetRelearning = limit - targetNew - targetLearning;

        var selected = new ArrayList<SongCandidate>(limit);
        takeOldest(byState.get(State.NEW), targetNew, selected);
        takeOldest(byState.get(State.LEARNING), targetLearning, selected);
        takeOldest(byState.get(State.RE_LEARNING), targetRelearning, selected);

        fillFromRemainingOldest(byState, selected, limit);

        return selected.stream()
                .map(candidate -> VOCABULARY_API_MAPPER.toResponse(candidate.vocabulary()))
                .toList();
    }

    private void takeOldest(List<SongCandidate> source, int count, List<SongCandidate> selected) {
        if (source == null || source.isEmpty() || count <= 0) {
            return;
        }
        for (var candidate : source) {
            if (count <= 0) {
                break;
            }
            if (selected.contains(candidate)) {
                continue;
            }
            selected.add(candidate);
            count--;
        }
    }

    private void fillFromRemainingOldest(Map<State, ? extends List<SongCandidate>> byState,
                                         List<SongCandidate> selected,
                                         int limit) {
        if (selected.size() >= limit) {
            return;
        }

        var remaining = new ArrayList<SongCandidate>();
        addRemaining(byState.get(State.NEW), selected, remaining);
        addRemaining(byState.get(State.LEARNING), selected, remaining);
        addRemaining(byState.get(State.RE_LEARNING), selected, remaining);
        remaining.sort(songCandidateComparator());

        for (var candidate : remaining) {
            if (selected.size() >= limit) {
                break;
            }
            selected.add(candidate);
        }
    }

    private void addRemaining(List<SongCandidate> source,
                              List<SongCandidate> selected,
                              List<SongCandidate> sink) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (var candidate : source) {
            if (selected.contains(candidate)) {
                continue;
            }
            sink.add(candidate);
        }
    }

    private SongCandidate toSongCandidate(com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyFlashcardReviewRecord review,
                                          Vocabulary vocabulary) {
        if (review == null || vocabulary == null || review.fsrsState() == null) {
            return null;
        }
        if (review.fsrsState() != State.NEW
                && review.fsrsState() != State.LEARNING
                && review.fsrsState() != State.RE_LEARNING) {
            return null;
        }
        var createdAt = vocabulary.createdAt() == null ? Instant.EPOCH : vocabulary.createdAt();
        return new SongCandidate(review.fsrsState(), createdAt, vocabulary.id().id(), vocabulary);
    }

    private Comparator<SongCandidate> songCandidateComparator() {
        return Comparator
                .comparing(SongCandidate::createdAt)
                .thenComparing(SongCandidate::vocabularyId);
    }

    private int normalizeSongLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_SONG_SELECTION_LIMIT;
        }
        if (requestedLimit < 1) {
            return 1;
        }
        return Math.min(requestedLimit, MAX_SONG_SELECTION_LIMIT);
    }

    private record SongCandidate(
            State state,
            Instant createdAt,
            String vocabularyId,
            Vocabulary vocabulary
    ) {
    }
}
