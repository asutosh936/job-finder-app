package com.asutosh.jobtracker.repository;

import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;

    private Job newJob(String title, JobStatus status, int sortOrder) {
        Job job = new Job();
        job.setTitle(title);
        job.setCompany("Acme Corp");
        job.setLocation("Remote");
        job.setSourceUrl("https://example.com/job");
        job.setDescription("A great job opportunity.");
        job.setStatus(status);
        job.setSortOrder(sortOrder);
        return job;
    }

    @Test
    void savingJobPopulatesTimestamps() {
        Job job = newJob("Backend Engineer", JobStatus.WISHLIST, 0);

        Job saved = jobRepository.saveAndFlush(job);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatingJobRefreshesUpdatedAt() {
        Job saved = jobRepository.saveAndFlush(newJob("Backend Engineer", JobStatus.WISHLIST, 0));
        var originalUpdatedAt = saved.getUpdatedAt();

        saved.setTitle("Senior Backend Engineer");
        Job updated = jobRepository.saveAndFlush(saved);

        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        assertThat(updated.getTitle()).isEqualTo("Senior Backend Engineer");
    }

    @Test
    void findByStatusOrderBySortOrderAscReturnsJobsInOrder() {
        jobRepository.saveAndFlush(newJob("Job C", JobStatus.APPLIED, 2));
        jobRepository.saveAndFlush(newJob("Job A", JobStatus.APPLIED, 0));
        jobRepository.saveAndFlush(newJob("Job B", JobStatus.APPLIED, 1));
        jobRepository.saveAndFlush(newJob("Other column", JobStatus.OFFER, 0));

        List<Job> applied = jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.APPLIED);

        assertThat(applied).extracting(Job::getTitle)
                .containsExactly("Job A", "Job B", "Job C");
    }

    @Test
    void findByStatusOrderBySortOrderAscReturnsEmptyListWhenNoMatches() {
        List<Job> rejected = jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.REJECTED);

        assertThat(rejected).isEmpty();
    }
}
