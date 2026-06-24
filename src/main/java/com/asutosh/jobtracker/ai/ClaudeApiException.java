package com.asutosh.jobtracker.ai;

/**
 * Raised when the Claude API returns an unusable response (empty content,
 * unparsable JSON, transport error, etc.).
 */
public class ClaudeApiException extends RuntimeException {

    public ClaudeApiException(String message) {
        super(message);
    }

    public ClaudeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
