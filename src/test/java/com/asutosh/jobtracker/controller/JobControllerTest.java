package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.ai.ClaudeApiException;
import com.asutosh.jobtracker.dto.JobExtractionResult;
import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import com.asutosh.jobtracker.service.JobExtractionService;
import com.asutosh.jobtracker.service.JobService;
import com.asutosh.jobtracker.service.MarkdownService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JobExtractionService jobExtractionService;

    @MockitoBean
    private MarkdownService markdownService;

    @Test
    void newJobModalRendersModalFragment() throws Exception {
        mockMvc.perform(get("/jobs/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Job")))
                .andExpect(content().string(containsString("Paste URL")))
                .andExpect(content().string(containsString("Paste Text")));
    }

    @Test
    void previewFromUrlPopulatesFormWithExtractedFields() throws Exception {
        when(jobExtractionService.extractFromUrl("https://example.com/job"))
                .thenReturn(new JobExtractionResult("Backend Engineer", "Acme", "Remote", "Build things"));

        mockMvc.perform(post("/jobs/preview")
                        .param("mode", "url")
                        .param("url", "https://example.com/job"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend Engineer")))
                .andExpect(content().string(containsString("Acme")))
                .andExpect(content().string(containsString("Remote")))
                .andExpect(content().string(containsString("https://example.com/job")));
    }

    @Test
    void previewFromTextPopulatesFormWithExtractedFields() throws Exception {
        when(jobExtractionService.extractFromText("pasted text"))
                .thenReturn(new JobExtractionResult("Backend Engineer", "Acme", "", "Build things"));

        mockMvc.perform(post("/jobs/preview")
                        .param("mode", "text")
                        .param("text", "pasted text"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend Engineer")))
                .andExpect(content().string(containsString("Acme")));
    }

    @Test
    void previewFromUrlFallsBackToManualFormOnExtractionFailure() throws Exception {
        when(jobExtractionService.extractFromUrl(any())).thenThrow(new ClaudeApiException("boom"));

        mockMvc.perform(post("/jobs/preview")
                        .param("mode", "url")
                        .param("url", "https://example.com/job"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Could not extract job details automatically")));
    }

    @Test
    void previewFromTextFallsBackToManualFormOnExtractionFailurePreservingPastedText() throws Exception {
        when(jobExtractionService.extractFromText(any())).thenThrow(new ClaudeApiException("boom"));

        mockMvc.perform(post("/jobs/preview")
                        .param("mode", "text")
                        .param("text", "pasted text"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Could not extract job details automatically")))
                .andExpect(content().string(containsString("pasted text")));
    }

    @Test
    void createSavesJobAndRedirectsToBoard() throws Exception {
        mockMvc.perform(post("/jobs")
                        .param("title", "Backend Engineer")
                        .param("company", "Acme")
                        .param("location", "Remote")
                        .param("sourceUrl", "https://example.com/job")
                        .param("description", "Build things"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("HX-Redirect", "/"));

        verify(jobService).createJob(eq("Backend Engineer"), eq("Acme"), eq("Remote"), eq("https://example.com/job"), eq("Build things"));
    }

    @Test
    void moveUpdatesJobAndReturnsRefreshedCounts() throws Exception {
        Map<JobStatus, List<Job>> counts = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, List.of());
        }
        counts.put(JobStatus.APPLIED, List.of(new Job()));
        when(jobService.getJobsGroupedByStatus()).thenReturn(counts);

        mockMvc.perform(post("/jobs/1/move")
                        .param("status", "APPLIED")
                        .param("orderedIds", "3,1,2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"count-APPLIED\"")))
                .andExpect(content().string(containsString("hx-swap-oob=\"true\"")));

        verify(jobService).moveJob(eq(1L), eq(JobStatus.APPLIED), eq(List.of(3L, 1L, 2L)));
    }

    @Test
    void detailRendersJobWithoutExistingKit() throws Exception {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Backend Engineer");
        job.setCompany("Acme");
        job.setLocation("Remote");
        job.setDescription("Build things");
        when(jobService.getJob(1L)).thenReturn(job);
        when(jobService.getKit(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend Engineer")))
                .andExpect(content().string(containsString("Build things")))
                .andExpect(content().string(containsString("Generate Kit")))
                .andExpect(content().string(containsString("No kit generated yet")));
    }

    @Test
    void detailRendersExistingKitSections() throws Exception {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Backend Engineer");
        job.setCompany("Acme");
        when(jobService.getJob(1L)).thenReturn(job);

        GeneratedKit kit = new GeneratedKit();
        kit.setCoverLetter("Dear Hiring Manager");
        kit.setTailoredResume("Tailored resume text");
        kit.setInterviewQuestions(List.of("Why this company?"));
        kit.setCompanyBrief("Acme makes widgets");
        kit.setGeneratedAt(Instant.parse("2026-06-01T12:00:00Z"));
        when(jobService.getKit(1L)).thenReturn(Optional.of(kit));
        when(markdownService.renderToHtml("Dear Hiring Manager")).thenReturn("<p>Dear Hiring Manager</p>");
        when(markdownService.renderToHtml("Tailored resume text")).thenReturn("<p>Tailored resume text</p>");
        when(markdownService.renderToHtml("Acme makes widgets")).thenReturn("<p>Acme makes widgets</p>");

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Regenerate Kit")))
                .andExpect(content().string(containsString("<p>Dear Hiring Manager</p>")))
                .andExpect(content().string(containsString("<p>Tailored resume text</p>")))
                .andExpect(content().string(containsString("Why this company?")))
                .andExpect(content().string(containsString("<p>Acme makes widgets</p>")));
    }

    @Test
    void moveWithoutOrderedIdsPassesEmptyList() throws Exception {
        Map<JobStatus, List<Job>> counts = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            counts.put(status, List.of());
        }
        when(jobService.getJobsGroupedByStatus()).thenReturn(counts);

        mockMvc.perform(post("/jobs/1/move")
                        .param("status", "REJECTED"))
                .andExpect(status().isOk());

        verify(jobService).moveJob(eq(1L), eq(JobStatus.REJECTED), eq(List.of()));
    }
}
