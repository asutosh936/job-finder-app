package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void getProfileRendersResumeText() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("My existing resume text");
        when(profileService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("My existing resume text")))
                .andExpect(content().string(containsString("Master resume text")));
    }

    @Test
    void postWithResumeTextSavesAndRedirectsToBoard() throws Exception {
        mockMvc.perform(multipart("/profile")
                        .param("resumeText", "Updated resume text"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/"))
                .andExpect(content().string(containsString("Updated resume text")));

        verify(profileService).updateResumeText("Updated resume text");
    }

    @Test
    void postWithPdfFileExtractsTextAndSaves() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("resumeFile", "resume.pdf", "application/pdf", "pdf-bytes".getBytes());
        when(profileService.extractTextFromPdf(any(byte[].class))).thenReturn("Extracted resume text");

        mockMvc.perform(multipart("/profile")
                        .file(pdfFile)
                        .param("resumeText", ""))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/"))
                .andExpect(content().string(containsString("Extracted resume text")));

        verify(profileService).updateResumeText("Extracted resume text");
    }

    @Test
    void postWithUnparseablePdfShowsErrorAndKeepsExistingText() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile("resumeFile", "resume.pdf", "application/pdf", "pdf-bytes".getBytes());
        UserProfile existing = new UserProfile();
        existing.setMasterResumeText("Existing resume text");
        when(profileService.extractTextFromPdf(any(byte[].class))).thenThrow(new ProfileServiceException());
        when(profileService.getProfile()).thenReturn(existing);

        mockMvc.perform(multipart("/profile")
                        .file(pdfFile)
                        .param("resumeText", ""))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("HX-Redirect"))
                .andExpect(content().string(containsString("Failed to extract text from the uploaded PDF")))
                .andExpect(content().string(containsString("Existing resume text")));

        verify(profileService, org.mockito.Mockito.never()).updateResumeText(any());
    }

    /**
     * Lightweight stand-in for {@code com.asutosh.jobtracker.service.ProfileException}
     * used only to exercise the controller's generic catch block.
     */
    private static class ProfileServiceException extends RuntimeException {
    }
}
