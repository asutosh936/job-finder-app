package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.UserProfileRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserProfileRepository userProfileRepository;

    public ProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Returns the single user profile row, seeded at application startup.
     */
    public UserProfile getProfile() {
        return userProfileRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No user profile found"));
    }

    public UserProfile updateResumeText(String resumeText) {
        log.debug("Updating master resume text in profile");
        UserProfile profile = getProfile();
        profile.setMasterResumeText(resumeText);
        return userProfileRepository.save(profile);
    }

    /**
     * Extracts plain text from an uploaded resume PDF using PDFBox.
     */
    public String extractTextFromPdf(byte[] pdfBytes) {
        log.info("Extracting text from PDF via PDFBox");
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            throw new ProfileException("Failed to extract text from PDF", e);
        }
    }
}
