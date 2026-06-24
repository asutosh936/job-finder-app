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

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/jobs")
public class JobController {

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

        try {
            JobExtractionResult result = isUrlMode
                    ? jobExtractionService.extractFromUrl(url)
                    : jobExtractionService.extractFromText(text);
            model.addAttribute("title", result.title());
            model.addAttribute("company", result.company());
            model.addAttribute("location", result.location());
            model.addAttribute("description", result.description());
            model.addAttribute("error", null);
        } catch (Exception e) {
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
        jobService.createJob(title, company, location, sourceUrl, description);

        response.setHeader("HX-Redirect", "/");
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @PostMapping("/{id}/move")
    public String move(@PathVariable Long id,
                        @RequestParam JobStatus status,
                        @RequestParam(required = false) String orderedIds,
                        Model model) {
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
