package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyDetailSeed;
import com.myriadcode.languagelearner.language_learning_system.application.externals.VocabularyExtractionPromptApi;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.ContentGenerationJobService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionCandidate;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExtractionRequest;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionCandidateRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyExtractionRequestRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_CANDIDATE_EXTRACTION;
import static com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType.VOCABULARY_DETAIL_GENERATION;

@Service
public class VocabularyExtractionService {
    private final VocabularyExtractionRequestRepo requestRepo;
    private final VocabularyExtractionCandidateRepo candidateRepo;
    private final VocabularyRepo vocabularyRepo;
    private final ContentGenerationJobService jobService;
    private final VocabularyExtractionPromptApi promptApi;
    private final VocabularyOrchestrationService vocabularyService;
    private final VocabularyDetailValidator detailValidator = new VocabularyDetailValidator();

    public VocabularyExtractionService(
            VocabularyExtractionRequestRepo requestRepo,
            VocabularyExtractionCandidateRepo candidateRepo,
            VocabularyRepo vocabularyRepo,
            ContentGenerationJobService jobService,
            VocabularyExtractionPromptApi promptApi,
            VocabularyOrchestrationService vocabularyService) {
        this.requestRepo = requestRepo;
        this.candidateRepo = candidateRepo;
        this.vocabularyRepo = vocabularyRepo;
        this.jobService = jobService;
        this.promptApi = promptApi;
        this.vocabularyService = vocabularyService;
    }

    @Transactional
    public VocabularyExtractionRequest submit(String userId, String sourceText) {
        requireUserId(userId);
        if (sourceText == null || sourceText.isBlank()) throw new IllegalArgumentException("sourceText is required");
        var request = requestRepo.save(new VocabularyExtractionRequest(
                new VocabularyExtractionRequest.VocabularyExtractionRequestId(UUID.randomUUID().toString()),
                new UserId(userId), sourceText, Instant.now()));
        jobService.createOrReplace(userId, VOCABULARY_CANDIDATE_EXTRACTION);
        return request;
    }

    @Transactional(readOnly = true)
    public VocabularyCandidatePrompt prepareCandidatePrompt(String userId) {
        requireUserId(userId);
        jobService.require(userId, VOCABULARY_CANDIDATE_EXTRACTION);
        var request = requestRepo.findOldestByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No vocabulary extraction request found"));
        return new VocabularyCandidatePrompt(request.id().id(), promptApi.candidatePrompt(request.sourceText()));
    }

    @Transactional
    public VocabularyExtractionResult storeCandidates(String userId, VocabularyCandidateSelection selection) {
        requireUserId(userId);
        jobService.require(userId, VOCABULARY_CANDIDATE_EXTRACTION);
        if (selection == null || selection.requestId() == null || selection.requestId().isBlank()) {
            throw validation("requestId is required");
        }
        var request = requestRepo.findByIdAndUserId(selection.requestId(), userId)
                .orElseThrow(() -> validation("Unknown vocabulary extraction request"));
        if (selection.candidates() == null) throw validation("candidates is required");

        var existingVocabulary = vocabularyRepo.findByUserId(userId).stream()
                .map(value -> normalize(value.surface()))
                .collect(Collectors.toSet());
        var alreadyStored = candidateRepo.findByUserId(userId).stream()
                .map(VocabularyExtractionCandidate::normalizedSurface)
                .collect(Collectors.toSet());
        var unique = new LinkedHashMap<String, String>();
        for (var candidate : selection.candidates()) {
            String surface = candidate == null ? "" : clean(candidate.surface());
            String normalized = normalize(surface);
            if (!normalized.isBlank() && !existingVocabulary.contains(normalized) && !alreadyStored.contains(normalized)) {
                unique.putIfAbsent(normalized, surface);
            }
        }
        var now = Instant.now();
        var created = unique.entrySet().stream()
                .map(entry -> new VocabularyExtractionCandidate(
                        new VocabularyExtractionCandidate.VocabularyExtractionCandidateId(UUID.randomUUID().toString()),
                        new UserId(userId), entry.getValue(), entry.getKey(), null, now))
                .toList();
        candidateRepo.saveAll(created);
        requestRepo.delete(request.id());
        if (requestRepo.existsByUserId(userId)) {
            jobService.createOrReplace(userId, VOCABULARY_CANDIDATE_EXTRACTION);
        } else {
            jobService.delete(userId, VOCABULARY_CANDIDATE_EXTRACTION);
        }
        if (!created.isEmpty()) jobService.createOrReplace(userId, VOCABULARY_DETAIL_GENERATION);
        return new VocabularyExtractionResult(selection.requestId(), selection.candidates().size(), created.size(), 0);
    }

    @Transactional(readOnly = true)
    public String prepareDetailPrompt(String userId) {
        requireUserId(userId);
        jobService.require(userId, VOCABULARY_DETAIL_GENERATION);
        var pending = candidateRepo.findPendingByUserId(userId);
        if (pending.isEmpty()) throw new IllegalStateException("No pending vocabulary candidates found");
        var seeds = pending.stream().map(value -> new VocabularyDetailSeed(value.id().id(), value.surface())).toList();
        return promptApi.detailPrompt(seeds);
    }

    @Transactional
    public VocabularyExtractionResult storeDetails(String userId, VocabularyDetailSelection selection) {
        requireUserId(userId);
        jobService.require(userId, VOCABULARY_DETAIL_GENERATION);
        var pending = candidateRepo.findPendingByUserId(userId);
        if (pending.isEmpty()) throw validation("No pending vocabulary candidates found");
        var validated = detailValidator.validate(pending, selection);
        var byId = pending.stream().collect(Collectors.toMap(value -> value.id().id(), Function.identity()));
        var updated = new java.util.ArrayList<VocabularyExtractionCandidate>();
        for (var detail : validated) {
            var examples = detail.examples().stream()
                    .map(value -> new CreateGeneratedVocabularyCommand.ExampleSentence(
                            value.sentence().trim(), value.translation().trim()))
                    .toList();
            var response = vocabularyService.createGeneratedVocabulary(userId, new CreateGeneratedVocabularyCommand(
                    detail.candidate().surface(), detail.translation(), detail.entryKind(), detail.notes(), examples));
            updated.add(byId.get(detail.candidate().id().id()).markCreated(response.id()));
        }
        candidateRepo.saveAll(updated);
        if (candidateRepo.existsPendingByUserId(userId)) {
            jobService.createOrReplace(userId, VOCABULARY_DETAIL_GENERATION);
        } else {
            jobService.delete(userId, VOCABULARY_DETAIL_GENERATION);
        }
        return new VocabularyExtractionResult(null, validated.size(), 0, updated.size());
    }

    private String clean(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }
    private String normalize(String value) { return clean(value).toLowerCase(Locale.ROOT); }
    private void requireUserId(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId is required");
    }
    private VocabularyExtractionValidationException validation(String error) {
        return new VocabularyExtractionValidationException(List.of(error));
    }
}
