package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.ai.ClaudeApiException;
import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.service.JobService;
import com.asutosh.jobtracker.service.KitService;
import com.asutosh.jobtracker.service.MarkdownService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KitController.class)
class KitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KitService kitService;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private MarkdownService markdownService;

    @Test
    void generateRendersKitOnSuccess() throws Exception {
        Job job = new Job();
        job.setId(1L);

        GeneratedKit kit = new GeneratedKit();
        kit.setJob(job);
        kit.setCoverLetter("Dear Hiring Manager...");
        kit.setTailoredResume("## Experience");
        kit.setInterviewQuestions(List.of("Why this role?"));
        kit.setCompanyBrief("Acme makes widgets.");
        kit.setGeneratedAt(Instant.parse("2026-06-01T12:00:00Z"));

        when(jobService.getJob(1L)).thenReturn(job);
        when(kitService.generateKit(1L)).thenReturn(kit);
        when(markdownService.renderToHtml("Dear Hiring Manager...")).thenReturn("<p>Dear Hiring Manager...</p>");
        when(markdownService.renderToHtml("## Experience")).thenReturn("<h2>Experience</h2>");
        when(markdownService.renderToHtml("Acme makes widgets.")).thenReturn("<p>Acme makes widgets.</p>");

        mockMvc.perform(post("/jobs/1/kit/generate"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Regenerate Kit")))
                .andExpect(content().string(containsString("<p>Dear Hiring Manager...</p>")))
                .andExpect(content().string(containsString("<h2>Experience</h2>")))
                .andExpect(content().string(containsString("Why this role?")))
                .andExpect(content().string(containsString("<p>Acme makes widgets.</p>")));
    }

    @Test
    void generateShowsErrorAndKeepsExistingKitOnFailure() throws Exception {
        Job job = new Job();
        job.setId(1L);

        GeneratedKit existing = new GeneratedKit();
        existing.setJob(job);
        existing.setCoverLetter("Existing cover letter");
        existing.setTailoredResume("Existing resume");
        existing.setInterviewQuestions(List.of("Existing question"));
        existing.setCompanyBrief("Existing brief");
        existing.setGeneratedAt(Instant.parse("2026-06-01T12:00:00Z"));

        when(jobService.getJob(1L)).thenReturn(job);
        when(kitService.generateKit(1L)).thenThrow(new ClaudeApiException("boom"));
        when(jobService.getKit(1L)).thenReturn(Optional.of(existing));
        when(markdownService.renderToHtml(any())).thenReturn("<p>rendered</p>");

        mockMvc.perform(post("/jobs/1/kit/generate"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Failed to generate the application kit")))
                .andExpect(content().string(containsString("Existing question")));
    }
}
