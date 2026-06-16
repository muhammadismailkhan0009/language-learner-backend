package com.myriadcode.languagelearner.common.enums;

import java.util.Arrays;

public enum LanguageLevel {
    A1,
    A2,
    B1,
    B2,
    C1;

    public static LanguageLevel from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Language level is required");
        }
        var normalized = raw.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(level -> level.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported language level: " + raw));
    }

    public static LanguageLevel defaultLevel() {
        return A1;
    }
}
