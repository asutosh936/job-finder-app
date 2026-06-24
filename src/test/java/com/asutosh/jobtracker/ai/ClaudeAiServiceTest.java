package com.asutosh.jobtracker.ai;

import com.asutosh.jobtracker.dto.GeneratedKitResult;
import com.asutosh.jobtracker.dto.JobExtractionResult;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.UserProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaudeAiServiceTest {

    private final ClaudeClient claudeClient = mock(ClaudeClient.class);
    private final ClaudeAiService claudeAiService = new ClaudeAiService(claudeClient);

    @Test
    void extractJobFieldsParsesPlainJsonResponse() {
        when(claudeClient.sendMessage(any())).thenReturn("""
                {"title": "Backend Engineer", "company": "Acme", "location": "Remote", "description": "Build things"}
                """);

        JobExtractionResult result = claudeAiService.extractJobFields("raw posting text");

        assertThat(result.title()).isEqualTo("Backend Engineer");
        assertThat(result.company()).isEqualTo("Acme");
        assertThat(result.location()).isEqualTo("Remote");
        assertThat(result.description()).isEqualTo("Build things");
    }

    @Test
    void extractJobFieldsStripsMarkdownCodeFences() {
        when(claudeClient.sendMessage(any())).thenReturn("""
                Here is the JSON:
                ```json
                {"title": "Backend Engineer", "company": "Acme", "location": "", "description": "Build things"}
                ```
                """);

        JobExtractionResult result = claudeAiService.extractJobFields("raw posting text");

        assertThat(result.title()).isEqualTo("Backend Engineer");
        assertThat(result.location()).isEmpty();
    }

    @Test
    void extractJobFieldsThrowsWhenNoJsonObjectFound() {
        when(claudeClient.sendMessage(any())).thenReturn("Sorry, I can't help with that.");

        assertThatThrownBy(() -> claudeAiService.extractJobFields("raw posting text"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("No JSON object found");
    }

    @Test
    void extractJobFieldsThrowsWhenClosingBraceIsMissing() {
        when(claudeClient.sendMessage(any())).thenReturn("{\"title\": \"Backend Engineer\"");

        assertThatThrownBy(() -> claudeAiService.extractJobFields("raw posting text"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("No JSON object found");
    }

    @Test
    void extractJobFieldsThrowsWhenClosingBraceComesBeforeOpeningBrace() {
        when(claudeClient.sendMessage(any())).thenReturn("} some text {");

        assertThatThrownBy(() -> claudeAiService.extractJobFields("raw posting text"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("No JSON object found");
    }

    @Test
    void extractJobFieldsThrowsWhenJsonIsMalformed() {
        when(claudeClient.sendMessage(any())).thenReturn("{not valid json}");

        assertThatThrownBy(() -> claudeAiService.extractJobFields("raw posting text"))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void generateKitParsesJsonResponse() {
        when(claudeClient.sendMessage(any())).thenReturn("""
                {
                  "coverLetter": "Dear Hiring Manager...",
                  "tailoredResume": "## Experience\\n...",
                  "interviewQuestions": ["Why this role?", "Tell me about a project."],
                  "companyBrief": "Acme makes widgets."
                }
                """);

        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany("Acme");
        job.setDescription("Build things");

        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("Experienced engineer...");

        GeneratedKitResult result = claudeAiService.generateKit(job, profile);

        assertThat(result.coverLetter()).isEqualTo("Dear Hiring Manager...");
        assertThat(result.tailoredResume()).contains("## Experience");
        assertThat(result.interviewQuestions()).containsExactly("Why this role?", "Tell me about a project.");
        assertThat(result.companyBrief()).isEqualTo("Acme makes widgets.");
    }

    @Test
    void generateKitThrowsWhenJsonIsMalformed() {
        when(claudeClient.sendMessage(any())).thenReturn("{not valid json}");

        Job job = new Job();
        UserProfile profile = new UserProfile();

        assertThatThrownBy(() -> claudeAiService.generateKit(job, profile))
                .isInstanceOf(ClaudeApiException.class)
                .hasMessageContaining("Failed to parse kit generation response");
    }
}
