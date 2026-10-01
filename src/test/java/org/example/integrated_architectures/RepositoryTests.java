package org.example.integrated_architectures;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.example.integrated_architectures.sport.Sport;
import org.example.integrated_architectures.sport.SportRepository;
import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserRepository;
import org.example.integrated_architectures.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Each test runs in a transaction that is rolled back, so the database stays clean.
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
@Transactional
class RepositoryTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    SportRepository sportRepository;

    @Autowired
    CoachProfileRepository coachProfileRepository;

    @Test
    void adminIsCreatedByMigration() {
        User admin = userRepository.findByEmail("admin@sportcoach.local").orElseThrow();

        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void sportsAreSortedByName() {
        List<Sport> sports = sportRepository.findAllByOrderByNameAsc();

        assertThat(sports).extracting(Sport::getName).startsWith("Boxing", "Chess");
    }

    @Test
    void coachProfileIsSavedWithSports() {
        User coach = userRepository.save(
                new User("coach@test.com", "hash", "Ivan", "Petrenko", Role.COACH, UserStatus.PENDING));
        CoachProfile profile = new CoachProfile(coach, 5, new BigDecimal("25.00"));
        profile.getSports().addAll(sportRepository.findAllByOrderByNameAsc().subList(0, 2));
        coachProfileRepository.save(profile);

        CoachProfile found = coachProfileRepository.findByUserId(coach.getId()).orElseThrow();
        assertThat(found.getSports()).hasSize(2);
        assertThat(userRepository.findByRoleAndStatus(Role.COACH, UserStatus.PENDING)).contains(coach);
    }
}
