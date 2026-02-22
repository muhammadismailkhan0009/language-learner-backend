package com.myriadcode.languagelearner.language_learning_system.application.services.vocabulary;

import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchPrivateVocabularyApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.PrivateVocabularyRecord;
import com.myriadcode.languagelearner.language_learning_system.application.mappers.vocabulary.VocabularyFlashcardsApiMapper;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.springframework.stereotype.Service;

@Service
public class VocabularyFlashcardsAdapter implements FetchPrivateVocabularyApi {

    private static final VocabularyFlashcardsApiMapper VOCABULARY_FLASHCARDS_API_MAPPER =
            VocabularyFlashcardsApiMapper.INSTANCE;
    private final VocabularyRepo vocabularyRepo;

    public VocabularyFlashcardsAdapter(VocabularyRepo vocabularyRepo) {
        this.vocabularyRepo = vocabularyRepo;
    }

    @Override
    public PrivateVocabularyRecord getVocabularyRecord(String vocabularyId, String userId) {
        var vocabulary = vocabularyRepo.findByIdAndUserId(vocabularyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for this user"));
        return VOCABULARY_FLASHCARDS_API_MAPPER.toPrivateVocabularyRecord(vocabulary);
    }
}
