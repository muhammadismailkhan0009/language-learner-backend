package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.jpa;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.ContentGenerationJobType;

import java.io.Serializable;
import java.util.Objects;

public class ContentGenerationJobEntityId implements Serializable {
    private String userId;
    private ContentGenerationJobType type;

    public ContentGenerationJobEntityId() {}

    public ContentGenerationJobEntityId(String userId, ContentGenerationJobType type) {
        this.userId = userId;
        this.type = type;
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (!(value instanceof ContentGenerationJobEntityId that)) return false;
        return Objects.equals(userId, that.userId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type);
    }
}
