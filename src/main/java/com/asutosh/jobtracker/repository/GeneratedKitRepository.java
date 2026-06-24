package com.asutosh.jobtracker.repository;

import com.asutosh.jobtracker.model.GeneratedKit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneratedKitRepository extends JpaRepository<GeneratedKit, Long> {

    Optional<GeneratedKit> findByJobId(Long jobId);
}
