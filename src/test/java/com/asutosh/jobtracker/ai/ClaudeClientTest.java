package com.asutosh.jobtracker.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class ClaudeClientTest {

    private MockRestServiceServer server;
    private ClaudeClient claudeClient;

    @BeforeEach
    void setUp() {
        ClaudeApiProperties properties = new ClaudeApiProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://api.anthropic.com");
        properties.setModel("claude-sonnet-4-6");
        properties.setVersion("2023-06-01");
        properties.setMaxTokens(1024);

        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        server = MockRestServiceServer.bindTo(builder).build();
        claudeClient = new ClaudeClient(builder.build(), properties);
    }

    @Test
    void sendMessageReturnsFirstContentBlockText() {
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {"content": [{"type": "text", "text": "hello world"}]}
                        """, MediaType.APPLICATION_JSON));

        String result = claudeClient.sendMessage("hi");

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void sendMessageThrowsWhenContentIsEmpty() {
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withSuccess("""
                        {"content": []}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> claudeClient.sendMessage("hi"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void sendMessageThrowsWhenContentIsNull() {
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withSuccess("""
                        {"content": null}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> claudeClient.sendMessage("hi"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void sendMessageThrowsWhenResponseBodyIsEmpty() {
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> claudeClient.sendMessage("hi"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void sendMessageThrowsOnTransportError() {
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> claudeClient.sendMessage("hi"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("Claude API request failed");
    }
}
