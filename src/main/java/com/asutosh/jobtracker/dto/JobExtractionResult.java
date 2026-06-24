package com.asutosh.jobtracker.dto;

/**
 * Structured job fields extracted from a job posting (URL or pasted text) by Claude.
 */
public record JobExtractionResult(String title, String company, String location, String description) {
}
