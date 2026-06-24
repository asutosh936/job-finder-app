package com.asutosh.jobtracker.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JobStatusTest {

    @Test
    void hasFivePipelineStages() {
        assertThat(JobStatus.values()).containsExactly(
                JobStatus.WISHLIST,
                JobStatus.APPLIED,
                JobStatus.INTERVIEWING,
                JobStatus.OFFER,
                JobStatus.REJECTED);
    }

    @Test
    void valueOfResolvesByName() {
        assertThat(JobStatus.valueOf("OFFER")).isEqualTo(JobStatus.OFFER);
    }
}
