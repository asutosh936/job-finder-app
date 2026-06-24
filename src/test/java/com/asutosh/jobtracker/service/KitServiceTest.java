package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.ai.ClaudeAiService;
import com.asutosh.jobtracker.dto.GeneratedKitResult;
import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.GeneratedKitRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KitServiceTest {

    private final JobService jobService = mock(JobService.class);
    private final ProfileService profileService = mock(ProfileService.class);
    private final ClaudeAiService claudeAiService = mock(ClaudeAiService.class);
    private final GeneratedKitRepository generatedKitRepository = mock(GeneratedKitRepository.class);
    private final KitService kitService = new KitService(jobService, profileService, claudeAiService, generatedKitRepository);

    @Test
    void generateKitCreatesNewKitWhenNoneExists() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Backend Engineer");
        job.setCompany("Acme");

        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("Experienced engineer...");

        when(jobService.getJob(1L)).thenReturn(job);
        when(profileService.getProfile()).thenReturn(profile);
        when(generatedKitRepository.findByJobId(1L)).thenReturn(Optional.empty());
        when(claudeAiService.generateKit(job, profile)).thenReturn(new GeneratedKitResult(
                "Dear Hiring Manager...", "## Experience...", List.of("Why this role?"), "Acme makes widgets."));
        when(generatedKitRepository.save(any(GeneratedKit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedKit kit = kitService.generateKit(1L);

        assertThat(kit.getJob()).isSameAs(job);
        assertThat(kit.getCoverLetter()).isEqualTo("Dear Hiring Manager...");
        assertThat(kit.getTailoredResume()).isEqualTo("## Experience...");
        assertThat(kit.getInterviewQuestions()).containsExactly("Why this role?");
        assertThat(kit.getCompanyBrief()).isEqualTo("Acme makes widgets.");
        assertThat(kit.getGeneratedAt()).isNotNull();
    }

    @Test
    void generateKitOverwritesExistingKit() {
        Job job = new Job();
        job.setId(1L);

        UserProfile profile = new UserProfile();

        GeneratedKit existing = new GeneratedKit();
        existing.setId(7L);
        existing.setCoverLetter("Old cover letter");
        existing.getInterviewQuestions().add("Old question");

        when(jobService.getJob(1L)).thenReturn(job);
        when(profileService.getProfile()).thenReturn(profile);
        when(generatedKitRepository.findByJobId(1L)).thenReturn(Optional.of(existing));
        when(claudeAiService.generateKit(job, profile)).thenReturn(new GeneratedKitResult(
                "New cover letter", "New resume", List.of("New question"), "New brief"));
        when(generatedKitRepository.save(any(GeneratedKit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GeneratedKit kit = kitService.generateKit(1L);

        assertThat(kit.getId()).isEqualTo(7L);
        assertThat(kit.getCoverLetter()).isEqualTo("New cover letter");
        assertThat(kit.getInterviewQuestions()).containsExactly("New question");
    }
}
