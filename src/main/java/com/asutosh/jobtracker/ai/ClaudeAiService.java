package com.asutosh.jobtracker.ai;

import com.asutosh.jobtracker.dto.GeneratedKitResult;
import com.asutosh.jobtracker.dto.JobExtractionResult;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;

/**
 * Higher-level Claude operations used by the job tracker.
 */
@Service
public class ClaudeAiService {

    private final ClaudeClient claudeClient;
    private final ObjectMapper objectMapper;

    public ClaudeAiService(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        this.objectMapper = JsonMapper.builder().build();
    }

    /**
     * Extracts structured job fields (title, company, location, description) from
     * raw job posting text using Claude, returning strict JSON parsed into a DTO.
     */
    public JobExtractionResult extractJobFields(String rawText) {
        String prompt = buildExtractionPrompt(rawText);
        String response = claudeClient.sendMessage(prompt);
        String json = extractJsonObject(response);

        try {
            return objectMapper.readValue(json, JobExtractionResult.class);
        } catch (Exception e) {
            throw new ClaudeApiException("Failed to parse job extraction response as JSON", e);
        }
    }

    /**
     * Generates a tailored application kit (cover letter, tailored resume, interview
     * questions, company brief) for the given job using the candidate's master resume.
     */
    public GeneratedKitResult generateKit(Job job, UserProfile profile) {
        String prompt = buildKitPrompt(job, profile);
        String response = claudeClient.sendMessage(prompt);
        String json = extractJsonObject(response);

        try {
            return objectMapper.readValue(json, GeneratedKitResult.class);
        } catch (Exception e) {
            throw new ClaudeApiException("Failed to parse kit generation response as JSON", e);
        }
    }

    private String buildKitPrompt(Job job, UserProfile profile) {
        return """
                You are an expert career coach helping a candidate apply for a job.

                Using the candidate's master resume and the job posting below, produce a
                tailored application kit. Respond with ONLY a JSON object (no markdown,
                no commentary) with exactly these keys:
                - "coverLetter": a tailored cover letter, written in Markdown, in the candidate's voice
                - "tailoredResume": the candidate's resume rewritten in Markdown to emphasize the experience most relevant to this role
                - "interviewQuestions": an array of exactly 10 likely interview questions for this role
                - "companyBrief": a one-page Markdown briefing on the company (mission, products, culture) based on the job posting

                Candidate's master resume:
                ---
                %s
                ---

                Job posting:
                Title: %s
                Company: %s
                Description:
                ---
                %s
                ---
                """.formatted(profile.getMasterResumeText(), job.getTitle(), job.getCompany(), job.getDescription());
    }

    private String buildExtractionPrompt(String rawText) {
        return """
                You are extracting structured fields from a job posting.

                Given the raw job posting text below, respond with ONLY a JSON object
                (no markdown, no commentary) with exactly these keys:
                - "title": the job title
                - "company": the company name
                - "location": the job location (or empty string if unknown)
                - "description": a cleaned-up version of the full job description

                Raw job posting text:
                ---
                %s
                ---
                """.formatted(rawText);
    }

    /**
     * Claude sometimes wraps JSON in markdown code fences or adds surrounding text;
     * this extracts the outermost {...} object.
     */
    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            throw new ClaudeApiException("No JSON object found in Claude response");
        }
        return text.substring(start, end + 1);
    }
}
