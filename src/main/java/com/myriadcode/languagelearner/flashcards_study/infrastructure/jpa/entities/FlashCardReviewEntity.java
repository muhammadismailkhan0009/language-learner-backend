package com.myriadcode.languagelearner.flashcards_study.infrastructure.jpa.entities;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "flashcard_review",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"language_content_id", "content_type","user_id","is_reversed"})
        })
public class FlashCardReviewEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, name = "language_content_id")
    private String languageContentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "content_type")
    private ContentRefType contentType;

    @Column(columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private String cardJson;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isReversed;

    public boolean getIsReversed() {
        return isReversed;
    }

    public void setIsReversed(boolean isReversed) {
        this.isReversed = isReversed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getLanguageContentId() {
        return languageContentId;
    }

    public void setLanguageContentId(String languageContentId) {
        this.languageContentId = languageContentId;
    }

    public ContentRefType getContentType() {
        return contentType;
    }

    public void setContentType(ContentRefType contentType) {
        this.contentType = contentType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardJson() {
        return cardJson;
    }

    public void setCardJson(String cardJson) {
        this.cardJson = cardJson;
    }
}
