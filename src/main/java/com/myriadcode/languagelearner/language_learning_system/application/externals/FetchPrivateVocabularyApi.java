package com.myriadcode.languagelearner.language_learning_system.application.externals;

public interface FetchPrivateVocabularyApi {

    PrivateVocabularyRecord getVocabularyRecord(String vocabularyId, String userId);

    java.util.List<PrivateVocabularyRecord> getVocabularyRecords(java.util.List<String> vocabularyIds, String userId);
}
