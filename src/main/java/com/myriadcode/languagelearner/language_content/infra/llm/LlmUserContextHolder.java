package com.myriadcode.languagelearner.language_content.infra.llm;

import java.util.Optional;

public final class LlmUserContextHolder {

    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();

    private LlmUserContextHolder() {
    }

    public static ScopedUser scoped(String userId) {
        CURRENT_USER_ID.set(userId);
        return () -> CURRENT_USER_ID.remove();
    }

    public static Optional<String> currentUserId() {
        return Optional.ofNullable(CURRENT_USER_ID.get());
    }

    @FunctionalInterface
    public interface ScopedUser extends AutoCloseable {
        @Override
        void close();
    }
}
