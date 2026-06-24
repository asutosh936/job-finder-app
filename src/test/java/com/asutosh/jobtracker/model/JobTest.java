package com.asutosh.jobtracker.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JobTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Job job = new Job();

        job.setId(1L);
        job.setTitle("Backend Engineer");
        job.setCompany("Acme Corp");
        job.setLocation("Remote");
        job.setSourceUrl("https://example.com/job");
        job.setDescription("Job description");
        job.setStatus(JobStatus.INTERVIEWING);
        job.setSortOrder(3);

        assertThat(job.getId()).isEqualTo(1L);
        assertThat(job.getTitle()).isEqualTo("Backend Engineer");
        assertThat(job.getCompany()).isEqualTo("Acme Corp");
        assertThat(job.getLocation()).isEqualTo("Remote");
        assertThat(job.getSourceUrl()).isEqualTo("https://example.com/job");
        assertThat(job.getDescription()).isEqualTo("Job description");
        assertThat(job.getStatus()).isEqualTo(JobStatus.INTERVIEWING);
        assertThat(job.getSortOrder()).isEqualTo(3);
    }

    @Test
    void defaultStatusIsWishlist() {
        Job job = new Job();

        assertThat(job.getStatus()).isEqualTo(JobStatus.WISHLIST);
    }

    @Test
    void onCreateSetsCreatedAndUpdatedTimestamps() {
        Job job = new Job();

        job.onCreate();

        assertThat(job.getCreatedAt()).isNotNull();
        assertThat(job.getUpdatedAt()).isNotNull();
        assertThat(job.getCreatedAt()).isEqualTo(job.getUpdatedAt());
    }

    @Test
    void onUpdateRefreshesUpdatedAtOnly() {
        Job job = new Job();
        job.onCreate();
        Instant createdAt = job.getCreatedAt();

        job.onUpdate();

        assertThat(job.getCreatedAt()).isEqualTo(createdAt);
        assertThat(job.getUpdatedAt()).isNotNull();
    }
}
