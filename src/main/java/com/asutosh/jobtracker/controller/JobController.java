package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.dto.JobExtractionResult;
import jakarta.servlet.http.HttpServletResponse;
import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.JobStatus;
import com.asutosh.jobtracker.service.JobExtractionService;
import com.asutosh.jobtracker.service.JobService;
import com.asutosh.jobtracker.service.MarkdownService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;
    private final JobExtractionService jobExtractionService;
    private final MarkdownService markdownService;

    public JobController(JobService jobService, JobExtractionService jobExtractionService, MarkdownService markdownService) {
        this.jobService = jobService;
        this.jobExtractionService = jobExtractionService;
        this.markdownService = markdownService;
    }

    @GetMapping("/new")
    public String newJobModal() {
        return "fragments/add-job-modal :: modal";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("Accessing detail view for job id {}", id);
        GeneratedKit kit = jobService.getKit(id).orElse(null);
        model.addAttribute("job", jobService.getJob(id));
        model.addAttribute("kit", kit);
        model.addAttribute("error", null);
        addRenderedMarkdown(model, kit);
        return "fragments/job-detail :: panel(job=${job}, kit=${kit})";
    }

    private void addRenderedMarkdown(Model model, GeneratedKit kit) {
        model.addAttribute("coverLetterHtml", kit == null ? null : markdownService.renderToHtml(kit.getCoverLetter()));
        model.addAttribute("tailoredResumeHtml", kit == null ? null : markdownService.renderToHtml(kit.getTailoredResume()));
        model.addAttribute("companyBriefHtml", kit == null ? null : markdownService.renderToHtml(kit.getCompanyBrief()));
    }

    @PostMapping("/preview")
    public String preview(@RequestParam String mode,
                           @RequestParam(required = false) String url,
                           @RequestParam(required = false) String text,
                           Model model) {
        boolean isUrlMode = "url".equals(mode);
        String sourceUrl = isUrlMode ? url : null;
        log.info("Previewing job, mode: {}, url: {}", mode, sourceUrl);

        try {
            JobExtractionResult result = isUrlMode
                    ? jobExtractionService.extractFromUrl(url)
                    : jobExtractionService.extractFromText(text);
            model.addAttribute("title", result.title());
            model.addAttribute("company", result.company());
            model.addAttribute("location", result.location());
            model.addAttribute("description", result.description());
            model.addAttribute("error", null);
            log.debug("Successfully extracted job details for title: {}", result.title());
        } catch (Exception e) {
            log.error("Failed to extract job details automatically", e);
            model.addAttribute("title", "");
            model.addAttribute("company", "");
            model.addAttribute("location", "");
            model.addAttribute("description", isUrlMode ? "" : text);
            model.addAttribute("error", "Could not extract job details automatically. Please fill in the fields manually.");
        }

        model.addAttribute("sourceUrl", sourceUrl);
        return "fragments/job-preview-form :: form";
    }

    @PostMapping
    public void create(@RequestParam String title,
                        @RequestParam String company,
                        @RequestParam(required = false) String location,
                        @RequestParam(required = false) String sourceUrl,
                        @RequestParam(required = false) String description,
                        HttpServletResponse response) {
        log.info("Creating new job with title: '{}' at company: '{}'", title, company);
        jobService.createJob(title, company, location, sourceUrl, description);

        response.setHeader("HX-Redirect", "/");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PostMapping("/{id}/move")
    public String move(@PathVariable Long id,
                        @RequestParam JobStatus status,
                        @RequestParam(required = false) String orderedIds,
                        Model model) {
        log.info("Moving job id {} to status {}", id, status);
        List<Long> ids = (orderedIds == null || orderedIds.isBlank())
                ? List.of()
                : Arrays.stream(orderedIds.split(","))
                        .map(Long::parseLong)
                        .toList();

        jobService.moveJob(id, status, ids);

        model.addAttribute("counts", jobService.getJobsGroupedByStatus());
        return "fragments/column-counts :: counts(counts=${counts})";
    }
}
