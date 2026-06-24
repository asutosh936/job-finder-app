package com.asutosh.jobtracker.dto;

import java.util.List;

/**
 * Structured application kit produced by Claude for a single job: a tailored cover
 * letter, a tailored resume, likely interview questions, and a company brief.
 */
public record GeneratedKitResult(String coverLetter, String tailoredResume, List<String> interviewQuestions, String companyBrief) {
}
