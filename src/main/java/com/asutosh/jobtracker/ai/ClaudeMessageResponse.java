package com.asutosh.jobtracker.ai;

import java.util.List;

/**
 * Minimal mapping of the Claude Messages API response — only the fields we need.
 */
public record ClaudeMessageResponse(List<ContentBlock> content) {

    public record ContentBlock(String type, String text) {
    }
}
