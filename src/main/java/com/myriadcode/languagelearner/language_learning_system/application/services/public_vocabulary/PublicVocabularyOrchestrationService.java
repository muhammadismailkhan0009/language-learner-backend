package com.myriadcode.languagelearner.language_learning_system.application.services.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.request.PublishPublicVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.response.PublicVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.vocabulary.response.VocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.public_vocabulary.PublicVocabularyApiMapper;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary.VocabularyApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.repo.PublicVocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.services.PublicVocabularyDomainService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.model.VocabularyExampleSentence;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.services.VocabularyDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicVocabularyOrchestrationService {

    private static final String ADMIN_KEY = "112233";
    private static final PublicVocabularyApiMapper PUBLIC_VOCABULARY_API_MAPPER = PublicVocabularyApiMapper.INSTANCE;
    private static final VocabularyApiMapper VOCABULARY_API_MAPPER = VocabularyApiMapper.INSTANCE;

    private final PublicVocabularyRepo publicVocabularyRepo;
    private final VocabularyRepo vocabularyRepo;

    public PublicVocabularyResponse publishVocabulary(String userId,
                                                      String vocabularyId,
                                                      PublishPublicVocabularyRequest request) {
        validateAdminKey(request.adminKey());

        var sourceVocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));

        var existingPublicRef = publicVocabularyRepo.findBySourceVocabularyId(vocabularyId);
        PublicVocabulary publicVocabulary;

        if (existingPublicRef.isPresent()) {
            publicVocabulary = PublicVocabularyDomainService.ensurePublished(existingPublicRef.get());
        } else {
            publicVocabulary = PublicVocabularyDomainService.publish(
                    sourceVocabulary.id(),
                    new UserId(userId)
            );
        }

        var saved = publicVocabularyRepo.save(publicVocabulary);
        return PUBLIC_VOCABULARY_API_MAPPER.toResponse(saved, sourceVocabulary);
    }

    public List<PublicVocabularyResponse> fetchPublicVocabularies() {
        var publicVocabularies = publicVocabularyRepo.findAllByStatus(PublicVocabulary.PublicVocabularyStatus.PUBLISHED);
        if (publicVocabularies.isEmpty()) {
            return List.of();
        }

        var sourceIds = publicVocabularies.stream().map(v -> v.sourceVocabularyId().id()).distinct().toList();
        var sourceVocabularies = vocabularyRepo.findByIds(sourceIds);
        var sourceById = sourceVocabularies.stream().collect(java.util.stream.Collectors.toMap(v -> v.id().id(), v -> v));

        return publicVocabularies.stream()
                .map(publicVocabulary -> {
                    var sourceVocabulary = sourceById.get(publicVocabulary.sourceVocabularyId().id());
                    return PUBLIC_VOCABULARY_API_MAPPER.toResponse(publicVocabulary, sourceVocabulary);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public VocabularyResponse addPublicVocabularyToPrivate(String userId, String publicVocabularyId) {
        var publicVocabulary = publicVocabularyRepo.findById(publicVocabularyId)
                .orElseThrow(() -> new IllegalArgumentException("Public vocabulary not found"));
        if (publicVocabulary.status() != PublicVocabulary.PublicVocabularyStatus.PUBLISHED) {
            throw new IllegalArgumentException("Public vocabulary is not published");
        }

        var sourceVocabulary = vocabularyRepo.findById(publicVocabulary.sourceVocabularyId().id())
                .orElseThrow(() -> new IllegalArgumentException("Source vocabulary not found"));

        var existing = vocabularyRepo.findByUserId(userId).stream()
                .filter(vocabulary -> vocabulary.entryKind() == sourceVocabulary.entryKind())
                .filter(vocabulary -> vocabulary.surface().equals(sourceVocabulary.surface()))
                .filter(vocabulary -> vocabulary.translation().equals(sourceVocabulary.translation()))
                .findFirst();

        if (existing.isPresent()) {
            return VOCABULARY_API_MAPPER.toResponse(existing.get());
        }

        var copied = VocabularyDomainService.create(
                new UserId(userId),
                sourceVocabulary.surface(),
                sourceVocabulary.translation(),
                sourceVocabulary.entryKind(),
                sourceVocabulary.notes(),
                sourceVocabulary.exampleSentences().stream()
                        .map(sentence -> new VocabularyExampleSentence(
                                null,
                                sentence.sentence(),
                                sentence.translation()
                        ))
                        .toList()
        );

        var saved = vocabularyRepo.save(copied);
        return VOCABULARY_API_MAPPER.toResponse(saved);
    }

    private void validateAdminKey(String adminKey) {
        if (adminKey == null || !ADMIN_KEY.equals(adminKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }
    }
}
