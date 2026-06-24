package com.asutosh.jobtracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * The single-user profile, holding the master resume text used as the basis
 * for every AI-tailored generation.
 */
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "master_resume_text", columnDefinition = "CLOB")
    private String masterResumeText;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserProfile() {
    }

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMasterResumeText() {
        return masterResumeText;
    }

    public void setMasterResumeText(String masterResumeText) {
        this.masterResumeText = masterResumeText;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
