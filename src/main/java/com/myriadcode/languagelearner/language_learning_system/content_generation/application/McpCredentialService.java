package com.myriadcode.languagelearner.language_learning_system.content_generation.application;

import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.model.McpCredential;
import com.myriadcode.languagelearner.language_learning_system.content_generation.domain.repo.McpCredentialRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Base64;
import java.util.function.Supplier;

@Service
public class McpCredentialService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    // Temporary development secret. Move to external secret management before production use.
    private static final String KEY_GENERATION_SECRET = "language-learner-mcp-development-secret";

    private final McpCredentialRepo credentialRepo;
    private final String publicUrl;
    private final Clock clock;
    private final Supplier<String> keyGenerator;

    @Autowired
    public McpCredentialService(
            McpCredentialRepo credentialRepo,
            @Value("${mcp.public-url:http://localhost:8080/mcp}") String publicUrl
    ) {
        this(credentialRepo, publicUrl, Clock.systemUTC(), McpCredentialService::generateKey);
    }

    McpCredentialService(
            McpCredentialRepo credentialRepo,
            String publicUrl,
            Clock clock,
            Supplier<String> keyGenerator
    ) {
        this.credentialRepo = credentialRepo;
        this.publicUrl = publicUrl;
        this.clock = clock;
        this.keyGenerator = keyGenerator;
    }

    @Transactional
    public String getOrCreateMcpUrl(String userId) {
        var normalizedUserId = requireValue(userId, "User id is required");
        var credential = credentialRepo.findByUserId(normalizedUserId)
                .orElseGet(() -> credentialRepo.save(new McpCredential(
                        normalizedUserId,
                        keyGenerator.get(),
                        clock.instant()
                )));
        return UriComponentsBuilder.fromUriString(publicUrl)
                .queryParam("key", credential.secretKey())
                .build()
                .toUriString();
    }

    @Transactional(readOnly = true)
    public String requireUserId(String secretKey) {
        var normalizedKey = requireValue(secretKey, "MCP key is required");
        return credentialRepo.findBySecretKey(normalizedKey)
                .map(McpCredential::userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid MCP key"));
    }

    private static String generateKey() {
        try {
            var randomBytes = new byte[32];
            SECURE_RANDOM.nextBytes(randomBytes);
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(KEY_GENERATION_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256"));
            return "llmcp_" + Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(randomBytes));
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to generate MCP key", exception);
        }
    }

    private String requireValue(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
