package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.ai.ClaudeAiService;
import com.asutosh.jobtracker.dto.JobExtractionResult;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobExtractionServiceTest {

    private final ClaudeAiService claudeAiService = mock(ClaudeAiService.class);
    private final JobExtractionService jobExtractionService = new JobExtractionService(claudeAiService);

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void extractFromTextDelegatesToClaudeAiService() {
        JobExtractionResult expected = new JobExtractionResult("Backend Engineer", "Acme", "Remote", "Build things");
        when(claudeAiService.extractJobFields("pasted text")).thenReturn(expected);

        JobExtractionResult result = jobExtractionService.extractFromText("pasted text");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void extractReadableTextStripsScriptsStylesAndChrome() {
        String html = """
                <html>
                <head><style>.x { color: red; }</style></head>
                <body>
                  <header>Site Header</header>
                  <nav>Nav Links</nav>
                  <script>alert('x');</script>
                  <main><h1>Backend Engineer</h1><p>Job description text.</p></main>
                  <footer>Footer Links</footer>
                </body>
                </html>
                """;

        String text = jobExtractionService.extractReadableText(html);

        assertThat(text).contains("Backend Engineer", "Job description text.");
        assertThat(text).doesNotContain("Site Header", "Nav Links", "Footer Links", "alert");
    }

    @Test
    void fetchHtmlRetrievesPageContentFromUrl() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/job", exchange -> {
            byte[] body = "<html><body><h1>Job Posting</h1></body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        String url = "http://localhost:" + server.getAddress().getPort() + "/job";
        String html = jobExtractionService.fetchHtml(url);

        assertThat(html).contains("Job Posting");
    }

    @Test
    void fetchHtmlThrowsJobExtractionExceptionOnFailure() {
        assertThatThrownBy(() -> jobExtractionService.fetchHtml("http://localhost:1/unreachable"))
                .isInstanceOf(JobExtractionException.class)
                .hasMessageContaining("Failed to fetch job posting URL");
    }

    @Test
    void extractFromUrlFetchesAndExtractsFields() throws IOException {
        JobExtractionResult expected = new JobExtractionResult("Backend Engineer", "Acme", "Remote", "Build things");
        when(claudeAiService.extractJobFields(any())).thenReturn(expected);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/job", exchange -> {
            byte[] body = "<html><body><h1>Backend Engineer</h1></body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        String url = "http://localhost:" + server.getAddress().getPort() + "/job";
        JobExtractionResult result = jobExtractionService.extractFromUrl(url);

        assertThat(result).isEqualTo(expected);
        verify(claudeAiService).extractJobFields(any());
    }
}
