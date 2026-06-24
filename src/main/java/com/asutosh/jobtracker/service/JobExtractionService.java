package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.ai.ClaudeAiService;
import com.asutosh.jobtracker.dto.JobExtractionResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Turns a job posting URL or pasted text into structured job fields via Claude.
 */
@Service
public class JobExtractionService {

    private final ClaudeAiService claudeAiService;

    public JobExtractionService(ClaudeAiService claudeAiService) {
        this.claudeAiService = claudeAiService;
    }

    /**
     * Extracts job fields directly from pasted text.
     */
    public JobExtractionResult extractFromText(String text) {
        return claudeAiService.extractJobFields(text);
    }

    /**
     * Fetches the given URL, strips it down to readable text, and extracts job fields.
     */
    public JobExtractionResult extractFromUrl(String url) {
        String html = fetchHtml(url);
        String readableText = extractReadableText(html);
        return claudeAiService.extractJobFields(readableText);
    }

    String fetchHtml(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; JobTrackerBot/1.0)")
                    .timeout(10_000)
                    .get()
                    .html();
        } catch (IOException e) {
            throw new JobExtractionException("Failed to fetch job posting URL: " + url, e);
        }
    }

    /**
     * Strips scripts, styles, and chrome elements, returning the remaining visible text.
     */
    String extractReadableText(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("script, style, noscript, nav, footer, header, svg").remove();
        return doc.body().text();
    }
}
