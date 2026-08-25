package com.myriadcode.languagelearner.language_learning_system.content_generation.application;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.McpCredential;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.McpCredentialRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class McpCredentialServiceTests {
    private final InMemoryCredentialRepo repo = new InMemoryCredentialRepo();
    private final McpCredentialService service = new McpCredentialService(
            repo,
            "https://example.test/mcp",
            Clock.fixed(Instant.parse("2026-08-25T12:00:00Z"), ZoneOffset.UTC),
            () -> "llmcp_test-key"
    );

    @Test
    void createsReusableMcpUrlForUser() {
        assertThat(service.getOrCreateMcpUrl("user-1"))
                .isEqualTo("https://example.test/mcp?key=llmcp_test-key");
        assertThat(service.getOrCreateMcpUrl("user-1"))
                .isEqualTo("https://example.test/mcp?key=llmcp_test-key");
        assertThat(repo.credentials).hasSize(1);
    }

    @Test
    void resolvesOnlyCredentialOwnerFromKey() {
        service.getOrCreateMcpUrl("user-1");
        assertThat(service.requireUserId("llmcp_test-key")).isEqualTo("user-1");
    }

    @Test
    void rejectsUnknownKey() {
        assertThatThrownBy(() -> service.requireUserId("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid MCP key");
    }

    private static final class InMemoryCredentialRepo implements McpCredentialRepo {
        private final Map<String, McpCredential> credentials = new HashMap<>();

        @Override
        public McpCredential save(McpCredential credential) {
            credentials.put(credential.userId(), credential);
            return credential;
        }

        @Override
        public Optional<McpCredential> findByUserId(String userId) {
            return Optional.ofNullable(credentials.get(userId));
        }

        @Override
        public Optional<McpCredential> findBySecretKey(String secretKey) {
            return credentials.values().stream()
                    .filter(credential -> credential.secretKey().equals(secretKey))
                    .findFirst();
        }
    }
}
