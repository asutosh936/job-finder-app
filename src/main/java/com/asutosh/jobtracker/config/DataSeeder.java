package com.asutosh.jobtracker.config;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Ensures the single {@link UserProfile} row exists on startup, since this is
 * a single-user application with no registration flow.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserProfileRepository userProfileRepository;

    public DataSeeder(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public void run(String... args) {
        if (userProfileRepository.count() == 0) {
            UserProfile profile = new UserProfile();
            profile.setMasterResumeText("");
            userProfileRepository.save(profile);
        }
    }
}
