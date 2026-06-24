package com.asutosh.jobtracker.service;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.UserProfileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceTest {

    private final UserProfileRepository userProfileRepository = mock(UserProfileRepository.class);
    private final ProfileService profileService = new ProfileService(userProfileRepository);

    @Test
    void getProfileReturnsFirstRow() {
        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("existing resume");
        when(userProfileRepository.findAll()).thenReturn(List.of(profile));

        assertThat(profileService.getProfile()).isSameAs(profile);
    }

    @Test
    void getProfileThrowsWhenNoRowExists() {
        when(userProfileRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(profileService::getProfile)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No user profile found");
    }

    @Test
    void updateResumeTextSavesNewText() {
        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("old");
        when(userProfileRepository.findAll()).thenReturn(List.of(profile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile updated = profileService.updateResumeText("new resume text");

        assertThat(updated.getMasterResumeText()).isEqualTo("new resume text");
        verify(userProfileRepository).save(profile);
    }

    @Test
    void extractTextFromPdfReturnsPageText() throws IOException {
        byte[] pdfBytes = createPdfWithText("Hello Resume");

        String text = profileService.extractTextFromPdf(pdfBytes);

        assertThat(text).contains("Hello Resume");
    }

    @Test
    void extractTextFromPdfThrowsOnInvalidPdf() {
        byte[] invalidBytes = "not a pdf".getBytes();

        assertThatThrownBy(() -> profileService.extractTextFromPdf(invalidBytes))
                .isInstanceOf(ProfileException.class)
                .hasMessageContaining("Failed to extract text from PDF");
    }

    private byte[] createPdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        }
    }
}
