package com.myriadcode.languagelearner.language_content.application.externals;

import com.myriadcode.languagelearner.common.ids.UserId;

public interface FetchLanguageContentApi {

    ChunkRecord getChunkRecord(String chunkId);

    SentenceRecord getSentenceRecord(String sentenceId);

    void generateCardsForUser(UserId userId);
}
