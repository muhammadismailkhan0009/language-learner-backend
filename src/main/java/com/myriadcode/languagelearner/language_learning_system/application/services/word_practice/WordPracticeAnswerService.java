package com.myriadcode.languagelearner.language_learning_system.application.services.word_practice;

import com.myriadcode.fsrs.api.enums.Rating;
import com.myriadcode.languagelearner.language_learning_system.application.externals.FetchVocabularyFlashcardReviewsApi;
import com.myriadcode.languagelearner.language_learning_system.application.externals.ReviewVocabularyFlashcardApi;
import com.myriadcode.languagelearner.language_learning_system.domain.word_practice.repo.WordPracticeRepo;
import com.myriadcode.languagelearner.language_learning_system.domain.vocabulary.repo.VocabularyRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WordPracticeAnswerService {
    private final WordPracticeRepo practiceRepo;
    private final VocabularyRepo vocabularyRepo;
    private final FetchVocabularyFlashcardReviewsApi flashcardReviewsApi;
    private final ReviewVocabularyFlashcardApi reviewApi;

    public WordPracticeAnswerService(WordPracticeRepo practiceRepo,
                                     VocabularyRepo vocabularyRepo,
                                     FetchVocabularyFlashcardReviewsApi flashcardReviewsApi,
                                     ReviewVocabularyFlashcardApi reviewApi) {
        this.practiceRepo = practiceRepo;
        this.vocabularyRepo = vocabularyRepo;
        this.flashcardReviewsApi = flashcardReviewsApi;
        this.reviewApi = reviewApi;
    }

    @Transactional
    public WordPracticeAnswerResult submit(String userId, String practiceId, String answer) {
        requireNonblank(userId, "userId");
        requireNonblank(practiceId, "practiceId");
        var practice = practiceRepo.findByIdAndUserId(practiceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Word Practice not found for user"));
        var vocabulary = vocabularyRepo.findByIdAndUserId(practice.vocabularyId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found for user"));
        var flashcard = flashcardReviewsApi.getVocabularyFlashcardsByUser(userId).stream()
                .filter(value -> value.isReversed() && practice.vocabularyId().equals(value.vocabularyId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary flashcard not found for user"));
        boolean correct = practice.isCorrect(answer) || matchesVocabularySurface(answer, vocabulary.surface());
        reviewApi.reviewVocabularyFlashcard(flashcard.flashcardId(), correct ? Rating.HARD : Rating.AGAIN);
        if (correct) {
            practiceRepo.deleteByIdAndUserId(practiceId, userId);
        }
        return new WordPracticeAnswerResult(correct, vocabulary.surface());
    }

    private boolean matchesVocabularySurface(String submittedAnswer, String vocabularySurface) {
        return submittedAnswer != null
                && !submittedAnswer.isBlank()
                && vocabularySurface.equalsIgnoreCase(submittedAnswer.strip());
    }

    private void requireNonblank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }
}
