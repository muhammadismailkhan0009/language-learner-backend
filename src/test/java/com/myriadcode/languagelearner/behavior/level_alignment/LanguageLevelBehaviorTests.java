package com.myriadcode.languagelearner.behavior.level_alignment;

import com.myriadcode.languagelearner.common.enums.LanguageLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanguageLevelBehaviorTests {

    @Test
    @DisplayName("LanguageLevel: accepts supported CEFR levels case-insensitively")
    void acceptsSupportedLevels() {
        assertThat(LanguageLevel.from("A1")).isEqualTo(LanguageLevel.A1);
        assertThat(LanguageLevel.from("a2")).isEqualTo(LanguageLevel.A2);
        assertThat(LanguageLevel.from(" B1 ")).isEqualTo(LanguageLevel.B1);
        assertThat(LanguageLevel.from("B2")).isEqualTo(LanguageLevel.B2);
        assertThat(LanguageLevel.from("C1")).isEqualTo(LanguageLevel.C1);
    }

    @Test
    @DisplayName("LanguageLevel: rejects blank and unsupported levels")
    void rejectsUnsupportedLevels() {
        assertThatThrownBy(() -> LanguageLevel.from(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
        assertThatThrownBy(() -> LanguageLevel.from("A1+"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported language level");
        assertThatThrownBy(() -> LanguageLevel.from("C2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported language level");
    }
}
