package com.myriadcode.languagelearner.language_content.domain.events;

import com.myriadcode.languagelearner.common.enums.ContentRefType;
import com.myriadcode.languagelearner.common.events.DomainEvent;

public class CreateCardEvent implements DomainEvent {

    private final String contentId;
    private final String userId;
    private final ContentRefType contentType;
    private final boolean isReversed;

    public CreateCardEvent(String contentId, String userId, ContentRefType contentType, boolean isReversed) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.userId = userId;
        this.isReversed = isReversed;
    }

    public String getContentId() {
        return contentId;
    }

    public ContentRefType getContentType() {
        return contentType;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isReversed() {
        return isReversed;
    }
}
