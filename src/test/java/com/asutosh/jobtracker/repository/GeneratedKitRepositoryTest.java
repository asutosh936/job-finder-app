package com.asutosh.jobtracker.repository;

import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GeneratedKitRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private GeneratedKitRepository generatedKitRepository;

    private Job persistJob() {
        Job job = new Job();
        job.setTitle("Backend Engineer");
        job.setCompany("Acme Corp");
        job.setStatus(JobStatus.WISHLIST);
        job.setSortOrder(0);
        return jobRepository.saveAndFlush(job);
    }

    @Test
    void savesAndFindsKitByJobId() {
        Job job = persistJob();

        GeneratedKit kit = new GeneratedKit();
        kit.setJob(job);
        kit.setCoverLetter("Dear Hiring Manager...");
        kit.setTailoredResume("Resume content");
        kit.setInterviewQuestions(List.of("Tell me about yourself", "Why this company?"));
        kit.setCompanyBrief("Acme Corp is a leader in widgets.");
        kit.setGeneratedAt(Instant.now());

        generatedKitRepository.saveAndFlush(kit);

        Optional<GeneratedKit> found = generatedKitRepository.findByJobId(job.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCoverLetter()).isEqualTo("Dear Hiring Manager...");
        assertThat(found.get().getTailoredResume()).isEqualTo("Resume content");
        assertThat(found.get().getInterviewQuestions()).containsExactly(
                "Tell me about yourself", "Why this company?");
        assertThat(found.get().getCompanyBrief()).isEqualTo("Acme Corp is a leader in widgets.");
        assertThat(found.get().getGeneratedAt()).isNotNull();
    }

    @Test
    void findByJobIdReturnsEmptyWhenNoKitExists() {
        Job job = persistJob();

        Optional<GeneratedKit> found = generatedKitRepository.findByJobId(job.getId());

        assertThat(found).isEmpty();
    }
}
