package com.asutosh.jobtracker.ai;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the Claude Messages API (POST /v1/messages).
 */
@Component
public class ClaudeClient {

    private final RestClient claudeRestClient;
    private final ClaudeApiProperties properties;

    public ClaudeClient(RestClient claudeRestClient, ClaudeApiProperties properties) {
        this.claudeRestClient = claudeRestClient;
        this.properties = properties;
    }

    /**
     * Sends a single user-role message and returns the text of the first content block.
     */
    public String sendMessage(String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "max_tokens", properties.getMaxTokens(),
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        ClaudeMessageResponse response;
        try {
            response = claudeRestClient.post()
                    .uri("/v1/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(ClaudeMessageResponse.class);
        } catch (Exception e) {
            throw new ClaudeApiException("Claude API request failed: " + e.getMessage(), e);
        }

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new ClaudeApiException("Claude API returned an empty response");
        }

        return response.content().get(0).text();
    }
}
