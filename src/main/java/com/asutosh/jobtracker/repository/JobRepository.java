package com.asutosh.jobtracker.repository;

import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusOrderBySortOrderAsc(JobStatus status);
}
