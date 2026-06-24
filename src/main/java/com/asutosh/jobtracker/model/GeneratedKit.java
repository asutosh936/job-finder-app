package com.asutosh.jobtracker.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The most recently generated "kit" of AI-produced application materials for a {@link Job}.
 * Regenerating overwrites the existing row.
 */
@Entity
@Table(name = "generated_kit")
public class GeneratedKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private Job job;

    @Lob
    @Column(name = "cover_letter", columnDefinition = "CLOB")
    private String coverLetter;

    @Lob
    @Column(name = "tailored_resume", columnDefinition = "CLOB")
    private String tailoredResume;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "generated_kit_interview_questions", joinColumns = @JoinColumn(name = "generated_kit_id"))
    @OrderColumn(name = "question_order")
    @Column(name = "question", columnDefinition = "CLOB")
    private List<String> interviewQuestions = new ArrayList<>();

    @Lob
    @Column(name = "company_brief", columnDefinition = "CLOB")
    private String companyBrief;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public GeneratedKit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getTailoredResume() {
        return tailoredResume;
    }

    public void setTailoredResume(String tailoredResume) {
        this.tailoredResume = tailoredResume;
    }

    public List<String> getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(List<String> interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }

    public String getCompanyBrief() {
        return companyBrief;
    }

    public void setCompanyBrief(String companyBrief) {
        this.companyBrief = companyBrief;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
