package com.myriadcode.languagelearner.language_learning_system.application.services.public_vocabulary;

import com.myriadcode.languagelearner.common.ids.UserId;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.request.PublishPublicVocabularyRequest;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.public_vocabulary.response.PublicVocabularyResponse;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.public_vocabulary.PublicVocabularyApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.model.PublicVocabulary;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.repo.PublicVocabularyRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.public_vocabulary.services.PublicVocabularyDomainService;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicVocabularyOrchestrationService {

    private static final String ADMIN_KEY = "112233";
    private static final PublicVocabularyApiMapper PUBLIC_VOCABULARY_API_MAPPER = PublicVocabularyApiMapper.INSTANCE;

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

    private void validateAdminKey(String adminKey) {
        if (adminKey == null || !ADMIN_KEY.equals(adminKey)) {
            throw new IllegalArgumentException("Invalid admin key");
        }
    }
}
