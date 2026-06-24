package com.asutosh.jobtracker.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownServiceTest {

    private final MarkdownService markdownService = new MarkdownService();

    @Test
    void rendersHeadingsAndEmphasis() {
        String html = markdownService.renderToHtml("# Hello\n\nThis is **bold** text.");

        assertThat(html).contains("<h1>Hello</h1>");
        assertThat(html).contains("<strong>bold</strong>");
    }

    @Test
    void returnsEmptyStringForNullOrBlankInput() {
        assertThat(markdownService.renderToHtml(null)).isEmpty();
        assertThat(markdownService.renderToHtml("   ")).isEmpty();
    }

    @Test
    void suppressesRawHtml() {
        String html = markdownService.renderToHtml("<script>alert('xss')</script>\n\nSome text");

        assertThat(html).doesNotContain("<script>alert('xss')</script>");
        assertThat(html).contains("Some text");
    }
}
