package com.myriadcode.languagelearner.language_content.domain.model;

import com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs.LangConfigsAdaptive;
import jakarta.validation.constraints.NotNull;

public record Chunk(ChunkId id, ChunkData data, @NotNull LangConfigsAdaptive langConfigsAdaptive) {
    public record ChunkId(String id) {
    }

    public record ChunkData(String chunk, String translation, String note) {
    }
}
