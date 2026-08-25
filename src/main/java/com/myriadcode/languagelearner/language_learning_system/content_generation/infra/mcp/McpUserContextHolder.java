package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

public final class McpUserContextHolder {
    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();

    private McpUserContextHolder() {
    }

    static ScopedUser scoped(String userId) {
        CURRENT_USER_ID.set(userId);
        return CURRENT_USER_ID::remove;
    }

    static String requireUserId() {
        var userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new IllegalStateException("MCP user context is unavailable");
        }
        return userId;
    }

    @FunctionalInterface
    interface ScopedUser extends AutoCloseable {
        @Override
        void close();
    }
}
