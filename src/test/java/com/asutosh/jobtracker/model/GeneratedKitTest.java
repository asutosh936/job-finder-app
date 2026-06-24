package com.asutosh.jobtracker.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedKitTest {

    @Test
    void gettersAndSettersRoundTrip() {
        GeneratedKit kit = new GeneratedKit();
        Job job = new Job();
        Instant now = Instant.now();

        kit.setId(1L);
        kit.setJob(job);
        kit.setCoverLetter("cover letter");
        kit.setTailoredResume("resume");
        kit.setInterviewQuestions(List.of("Q1", "Q2"));
        kit.setCompanyBrief("brief");
        kit.setGeneratedAt(now);

        assertThat(kit.getId()).isEqualTo(1L);
        assertThat(kit.getJob()).isSameAs(job);
        assertThat(kit.getCoverLetter()).isEqualTo("cover letter");
        assertThat(kit.getTailoredResume()).isEqualTo("resume");
        assertThat(kit.getInterviewQuestions()).containsExactly("Q1", "Q2");
        assertThat(kit.getCompanyBrief()).isEqualTo("brief");
        assertThat(kit.getGeneratedAt()).isEqualTo(now);
    }

    @Test
    void interviewQuestionsDefaultsToEmptyList() {
        GeneratedKit kit = new GeneratedKit();

        assertThat(kit.getInterviewQuestions()).isEmpty();
    }
}
