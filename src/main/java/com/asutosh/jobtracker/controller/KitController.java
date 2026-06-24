package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.service.JobService;
import com.asutosh.jobtracker.service.KitService;
import com.asutosh.jobtracker.service.MarkdownService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/jobs")
public class KitController {

    private static final Logger log = LoggerFactory.getLogger(KitController.class);

    private final KitService kitService;
    private final JobService jobService;
    private final MarkdownService markdownService;

    public KitController(KitService kitService, JobService jobService, MarkdownService markdownService) {
        this.kitService = kitService;
        this.jobService = jobService;
        this.markdownService = markdownService;
    }

    @PostMapping("/{id}/kit/generate")
    public String generate(@PathVariable Long id, Model model) {
        log.info("Generating application kit for job id {}", id);
        Job job = jobService.getJob(id);
        String error = null;
        GeneratedKit kit;

        try {
            kit = kitService.generateKit(id);
            log.info("Successfully generated kit for job id {}", id);
        } catch (Exception e) {
            log.error("Failed to generate application kit for job id " + id, e);
            kit = jobService.getKit(id).orElse(null);
            error = "Failed to generate the application kit. Please try again.";
        }

        model.addAttribute("job", job);
        model.addAttribute("kit", kit);
        model.addAttribute("error", error);
        model.addAttribute("coverLetterHtml", kit == null ? null : markdownService.renderToHtml(kit.getCoverLetter()));
        model.addAttribute("tailoredResumeHtml", kit == null ? null : markdownService.renderToHtml(kit.getTailoredResume()));
        model.addAttribute("companyBriefHtml", kit == null ? null : markdownService.renderToHtml(kit.getCompanyBrief()));
        return "fragments/kit-panel :: panel";
    }
}
