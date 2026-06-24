package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String profile(Model model) {
        model.addAttribute("resumeText", profileService.getProfile().getMasterResumeText());
        model.addAttribute("error", null);
        return "fragments/profile-panel :: panel";
    }

    @PostMapping
    public String save(@RequestParam(required = false) String resumeText,
                        @RequestParam(required = false) MultipartFile resumeFile,
                        Model model,
                        HttpServletResponse response) {
        String error = null;
        String text = resumeText == null ? "" : resumeText;

        if (resumeFile != null && !resumeFile.isEmpty()) {
            try {
                text = profileService.extractTextFromPdf(resumeFile.getBytes());
            } catch (Exception e) {
                error = "Failed to extract text from the uploaded PDF. Please paste your resume text instead.";
                text = profileService.getProfile().getMasterResumeText();
            }
        }

        if (error == null) {
            profileService.updateResumeText(text);
            response.setHeader("HX-Redirect", "/");
        }

        model.addAttribute("resumeText", text);
        model.addAttribute("error", error);
        return "fragments/profile-panel :: panel";
    }
}
