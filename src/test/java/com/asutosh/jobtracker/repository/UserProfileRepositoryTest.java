package com.asutosh.jobtracker.repository;

import com.asutosh.jobtracker.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void savingProfilePopulatesUpdatedAtAndPersistsResumeText() {
        UserProfile profile = new UserProfile();
        profile.setMasterResumeText("My resume content");

        UserProfile saved = userProfileRepository.saveAndFlush(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getMasterResumeText()).isEqualTo("My resume content");
    }

    @Test
    void updatingProfileRefreshesUpdatedAt() {
        UserProfile saved = userProfileRepository.saveAndFlush(new UserProfile());
        var originalUpdatedAt = saved.getUpdatedAt();

        saved.setMasterResumeText("Updated resume");
        UserProfile updated = userProfileRepository.saveAndFlush(saved);

        assertThat(updated.getMasterResumeText()).isEqualTo("Updated resume");
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
