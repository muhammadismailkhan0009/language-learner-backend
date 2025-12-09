package com.myriadcode.languagelearner.language_content.domain.model.language_settings.german.configs;

public enum ContentGenerationQuantity {
    SENTENCES(8);

    private final int number;

    ContentGenerationQuantity(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
