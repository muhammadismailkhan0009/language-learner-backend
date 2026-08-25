package com.myriadcode.languagelearner.language_learning_system.application.controllers.content_generation;

import com.myriadcode.languagelearner.common.dtos.ApiResponse;
import com.myriadcode.languagelearner.language_learning_system.application.controllers.content_generation.response.McpUrlResponse;
import com.myriadcode.languagelearner.language_learning_system.content_generation.application.McpCredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
@RequestMapping("api/v1/users/me/profile")
public class McpProfileController {
    private final McpCredentialService credentialService;

    public McpProfileController(McpCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("mcp-url")
    public ResponseEntity<ApiResponse<McpUrlResponse>> getOrCreateMcpUrl(
            @RequestParam("userId") String userId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(new McpUrlResponse(
                credentialService.getOrCreateMcpUrl(userId)
        )));
    }
}
