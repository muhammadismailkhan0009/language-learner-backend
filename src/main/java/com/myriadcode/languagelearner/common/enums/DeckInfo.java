package com.myriadcode.languagelearner.common.enums;

public enum DeckInfo {
    CHUNKS("chunks"),

    SENTENCES("sentences"),
    SENTENCES_REVISION("sentences");

    private final String id;

    DeckInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
