package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.model.GeneratedKit;
import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import com.asutosh.jobtracker.repository.GeneratedKitRepository;
import com.asutosh.jobtracker.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final GeneratedKitRepository generatedKitRepository;

    public JobService(JobRepository jobRepository, GeneratedKitRepository generatedKitRepository) {
        this.jobRepository = jobRepository;
        this.generatedKitRepository = generatedKitRepository;
    }

    /**
     * Returns all jobs grouped by status, in pipeline order, each list sorted by sortOrder.
     */
    public Map<JobStatus, List<Job>> getJobsGroupedByStatus() {
        Map<JobStatus, List<Job>> grouped = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            grouped.put(status, jobRepository.findByStatusOrderBySortOrderAsc(status));
        }
        return grouped;
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatusOrderBySortOrderAsc(status);
    }

    public Job getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
    }

    public Optional<GeneratedKit> getKit(Long jobId) {
        return generatedKitRepository.findByJobId(jobId);
    }

    /**
     * Creates and persists a new job at the end of the Wishlist column.
     */
    public Job createJob(String title, String company, String location, String sourceUrl, String description) {
        List<Job> wishlist = jobRepository.findByStatusOrderBySortOrderAsc(JobStatus.WISHLIST);
        int nextOrder = wishlist.isEmpty() ? 0 : wishlist.get(wishlist.size() - 1).getSortOrder() + 1;

        Job job = new Job();
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setSourceUrl(sourceUrl);
        job.setDescription(description);
        job.setStatus(JobStatus.WISHLIST);
        job.setSortOrder(nextOrder);
        return jobRepository.save(job);
    }

    /**
     * Moves a job to a new status column and resequences the target column's
     * sortOrder to match the given ordering of job ids (as dropped by the user).
     */
    @Transactional
    public void moveJob(Long jobId, JobStatus newStatus, List<Long> orderedIds) {
        Job job = getJob(jobId);
        job.setStatus(newStatus);
        jobRepository.save(job);

        int order = 0;
        for (Long id : orderedIds) {
            Job columnJob = getJob(id);
            columnJob.setSortOrder(order++);
            jobRepository.save(columnJob);
        }
    }
}
