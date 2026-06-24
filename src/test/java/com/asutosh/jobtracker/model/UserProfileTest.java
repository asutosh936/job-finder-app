package com.asutosh.jobtracker.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    @Test
    void gettersAndSettersRoundTrip() {
        UserProfile profile = new UserProfile();

        profile.setId(1L);
        profile.setMasterResumeText("resume text");

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getMasterResumeText()).isEqualTo("resume text");
    }

    @Test
    void touchSetsUpdatedAt() {
        UserProfile profile = new UserProfile();

        profile.touch();

        assertThat(profile.getUpdatedAt()).isNotNull();
    }
}
