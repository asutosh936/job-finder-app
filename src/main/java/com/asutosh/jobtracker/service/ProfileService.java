package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.UserProfileRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProfileService {

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
        UserProfile profile = getProfile();
        profile.setMasterResumeText(resumeText);
        return userProfileRepository.save(profile);
    }

    /**
     * Extracts plain text from an uploaded resume PDF using PDFBox.
     */
    public String extractTextFromPdf(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new ProfileException("Failed to extract text from PDF", e);
        }
    }
}
