package com.myriadcode.languagelearner.common.events;

public class ClearVocabularyClozeSentenceEvent implements DomainEvent {

    private final String vocabularyId;
    private final String userId;

    public ClearVocabularyClozeSentenceEvent(String vocabularyId, String userId) {
        this.vocabularyId = vocabularyId;
        this.userId = userId;
    }

    public String getVocabularyId() {
        return vocabularyId;
    }

    public String getUserId() {
        return userId;
    }
}
