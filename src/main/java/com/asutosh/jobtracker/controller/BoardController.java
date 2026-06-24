package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    private final JobService jobService;

    public BoardController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/")
    public String board(Model model) {
        model.addAttribute("jobsByStatus", jobService.getJobsGroupedByStatus());
        return "board";
    }
}
