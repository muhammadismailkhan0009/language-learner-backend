package com.myriadcode.languagelearner.common.events;

import com.myriadcode.languagelearner.common.enums.ContentRefType;

public class CreateFlashCardEvent implements DomainEvent {

    private final String contentId;
    private final String userId;
    private final ContentRefType contentType;
    private final boolean isReversed;

    public CreateFlashCardEvent(String contentId, String userId, ContentRefType contentType, boolean isReversed) {
        this.contentId = contentId;
        this.userId = userId;
        this.contentType = contentType;
        this.isReversed = isReversed;
    }

    public String getContentId() {
        return contentId;
    }

    public String getUserId() {
        return userId;
    }

    public ContentRefType getContentType() {
        return contentType;
    }

    public boolean isReversed() {
        return isReversed;
    }
}
