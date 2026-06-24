package com.asutosh.jobtracker.config;

import com.asutosh.jobtracker.model.UserProfile;
import com.asutosh.jobtracker.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSeederTest {

    @Test
    void seedsProfileWhenNoneExists() throws Exception {
        UserProfileRepository repository = mock(UserProfileRepository.class);
        when(repository.count()).thenReturn(0L);

        new DataSeeder(repository).run();

        verify(repository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void doesNotSeedProfileWhenOneAlreadyExists() throws Exception {
        UserProfileRepository repository = mock(UserProfileRepository.class);
        when(repository.count()).thenReturn(1L);

        new DataSeeder(repository).run();

        verify(repository, never()).save(any(UserProfile.class));
    }
}
