package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.AddVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.request.UpdateVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary.VocabularyApiMapper;
import com.myriadcode.languagelearner.language_learning_system.application.publishers.VocabularyFlashCardPublisher;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.Vocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularyOrchestrationService {

    private static final VocabularyApiMapper VOCABULARY_API_MAPPER = VocabularyApiMapper.INSTANCE;
    private final VocabularyRepo vocabularyRepo;
    private final VocabularyFlashCardPublisher vocabularyFlashCardPublisher;

    @Autowired
    public VocabularyOrchestrationService(VocabularyRepo vocabularyRepo,
                                          VocabularyFlashCardPublisher vocabularyFlashCardPublisher) {
        this.vocabularyRepo = vocabularyRepo;
        this.vocabularyFlashCardPublisher = vocabularyFlashCardPublisher;
    }

    // Backward-compatible constructor for unit tests with fake repos.
    public VocabularyOrchestrationService(VocabularyRepo vocabularyRepo) {
        this(vocabularyRepo, new VocabularyFlashCardPublisher(domainEvent -> {
        }));
    }

    public VocabularyResponse addVocabulary(String userId, AddVocabularyRequest request) {
        var toSave = VocabularyDomainService.create(
                new UserId(userId),
                request.surface(),
                request.translation(),
                request.entryKind(),
                request.notes(),
                VOCABULARY_API_MAPPER.toCreateSentences(request.exampleSentences())
        );
        var saved = vocabularyRepo.save(toSave);
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
        return VOCABULARY_API_MAPPER.toResponse(saved);
    }

    public void createFlashCardsForVocabulary(String userId, String vocabularyId) {
        var vocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        vocabularyFlashCardPublisher.createPrivateVocabularyCards(vocabulary);
    }

    public List<VocabularyResponse> fetchVocabularies(String userId) {
        return vocabularyRepo.findByUserId(userId).stream()
                .map(VOCABULARY_API_MAPPER::toResponse)
                .toList();
    }

    public VocabularyResponse fetchVocabulary(String userId, String vocabularyId) {
        var vocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        return VOCABULARY_API_MAPPER.toResponse(vocabulary);
    }
}
