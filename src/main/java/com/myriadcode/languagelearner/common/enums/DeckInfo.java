package com.myriadcode.languagelearner.common.enums;

public enum DeckInfo {
    CHUNKS("chunks"),
    SENTENCES("sentences");

    private final String id;

    DeckInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
