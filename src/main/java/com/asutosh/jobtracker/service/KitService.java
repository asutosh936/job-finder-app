package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.ai.ClaudeAiService;
import com.asutosh.jobtracker.dto.GeneratedKitResult;
import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.GeneratedKitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Generates and persists tailored application kits for jobs via Claude.
 */
@Service
public class KitService {

    private final JobService jobService;
    private final ProfileService profileService;
    private final ClaudeAiService claudeAiService;
    private final GeneratedKitRepository generatedKitRepository;

    public KitService(JobService jobService, ProfileService profileService,
                       ClaudeAiService claudeAiService, GeneratedKitRepository generatedKitRepository) {
        this.jobService = jobService;
        this.profileService = profileService;
        this.claudeAiService = claudeAiService;
        this.generatedKitRepository = generatedKitRepository;
    }

    /**
     * Generates a new kit for the given job, overwriting any existing kit.
     */
    @Transactional
    public GeneratedKit generateKit(Long jobId) {
        Job job = jobService.getJob(jobId);
        UserProfile profile = profileService.getProfile();

        GeneratedKitResult result = claudeAiService.generateKit(job, profile);

        GeneratedKit kit = generatedKitRepository.findByJobId(jobId).orElseGet(GeneratedKit::new);
        kit.setJob(job);
        kit.setCoverLetter(result.coverLetter());
        kit.setTailoredResume(result.tailoredResume());
        kit.getInterviewQuestions().clear();
        kit.getInterviewQuestions().addAll(result.interviewQuestions());
        kit.setCompanyBrief(result.companyBrief());
        kit.setGeneratedAt(Instant.now());

        return generatedKitRepository.save(kit);
    }
}
