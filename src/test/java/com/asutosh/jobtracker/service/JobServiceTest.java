package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import com.asutosh.jobtracker.repository.GeneratedKitRepository;
import com.asutosh.jobtracker.repository.JobRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobServiceTest {

    private final JobRepository jobRepository = mock(JobRepository.class);
    private final GeneratedKitRepository generatedKitRepository = mock(GeneratedKitRepository.class);
    private final JobService jobService = new JobService(jobRepository, generatedKitRepository);

    private Job job(String title, JobStatus status, int sortOrder) {
        Job job = new Job();
        job.setTitle(title);
        job.setStatus(status);
        job.setSortOrder(sortOrder);
        return job;
    }

    @Test
    void getJobsGroupedByStatusReturnsAllFiveStatusesInOrder() {
        for (JobStatus status : JobStatus.values()) {
            when(jobRepository.findByStatusOrderBySortOrderAsc(status)).thenReturn(List.of());
        }

        Map<JobStatus, List<Job>> grouped = jobService.getJobsGroupedByStatus();

        assertThat(grouped.keySet()).containsExactly(JobStatus.values());
        assertThat(grouped.get(JobStatus.WISHLIST)).isEmpty();
    }

    @Test
    void getJobsByStatusDelegatesToRepository() {
        Job job = job("Engineer", JobStatus.APPLIED, 0);
        when(jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.APPLIED)).thenReturn(List.of(job));

        assertThat(jobService.getJobsByStatus(JobStatus.APPLIED)).containsExactly(job);
    }

    @Test
    void getJobReturnsJobWhenFound() {
        Job job = job("Engineer", JobStatus.WISHLIST, 0);
        job.setId(5L);
        when(jobRepository.findById(5L)).thenReturn(Optional.of(job));

        assertThat(jobService.getJob(5L)).isSameAs(job);
    }

    @Test
    void getJobThrowsWhenNotFound() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJob(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getKitReturnsKitWhenPresent() {
        GeneratedKit kit = new GeneratedKit();
        when(generatedKitRepository.findByJobId(5L)).thenReturn(Optional.of(kit));

        assertThat(jobService.getKit(5L)).contains(kit);
    }

    @Test
    void getKitReturnsEmptyWhenAbsent() {
        when(generatedKitRepository.findByJobId(5L)).thenReturn(Optional.empty());

        assertThat(jobService.getKit(5L)).isEmpty();
    }

    @Test
    void createJobAppendsToEndOfWishlistWhenNotEmpty() {
        Job existing = job("Existing", JobStatus.WISHLIST, 4);
        when(jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.WISHLIST)).thenReturn(List.of(existing));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.createJob("Engineer", "Acme", "Remote", "https://example.com", "desc");

        assertThat(created.getStatus()).isEqualTo(JobStatus.WISHLIST);
        assertThat(created.getSortOrder()).isEqualTo(5);
        assertThat(created.getTitle()).isEqualTo("Engineer");
        assertThat(created.getCompany()).isEqualTo("Acme");
        assertThat(created.getLocation()).isEqualTo("Remote");
        assertThat(created.getSourceUrl()).isEqualTo("https://example.com");
        assertThat(created.getDescription()).isEqualTo("desc");
    }

    @Test
    void createJobStartsAtZeroWhenWishlistEmpty() {
        when(jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.WISHLIST)).thenReturn(List.of());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job created = jobService.createJob("Engineer", "Acme", null, null, null);

        assertThat(created.getSortOrder()).isEqualTo(0);
        verify(jobRepository).save(eq(created));
    }

    @Test
    void moveJobUpdatesStatusAndResequencesTargetColumn() {
        Job moved = job("Engineer", JobStatus.WISHLIST, 0);
        moved.setId(1L);
        Job sibling = job("Other", JobStatus.APPLIED, 0);
        sibling.setId(2L);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(moved));
        when(jobRepository.findById(2L)).thenReturn(Optional.of(sibling));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.moveJob(1L, JobStatus.APPLIED, List.of(2L, 1L));

        assertThat(moved.getStatus()).isEqualTo(JobStatus.APPLIED);
        assertThat(sibling.getSortOrder()).isEqualTo(0);
        assertThat(moved.getSortOrder()).isEqualTo(1);
    }

    @Test
    void moveJobWithEmptyOrderedIdsOnlyUpdatesStatus() {
        Job moved = job("Engineer", JobStatus.WISHLIST, 0);
        moved.setId(1L);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(moved));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobService.moveJob(1L, JobStatus.REJECTED, List.of());

        assertThat(moved.getStatus()).isEqualTo(JobStatus.REJECTED);
        verify(jobRepository).save(moved);
    }
}
