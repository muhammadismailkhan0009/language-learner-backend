package com.myriadcode.languagelearner.language_learning_system.content_generation.infra.mcp;

import com.myriadcode.languagelearner.language_learning_system.content_generation.application.McpCredentialService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
class McpKeyAuthenticationFilter extends OncePerRequestFilter {
    private final McpCredentialService credentialService;

    McpKeyAuthenticationFilter(McpCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/mcp");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String userId;
        try {
            userId = credentialService.requireUserId(request.getParameter("key"));
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid MCP key");
            return;
        }

        try (var ignored = McpUserContextHolder.scoped(userId)) {
            filterChain.doFilter(request, response);
        }
    }
}
