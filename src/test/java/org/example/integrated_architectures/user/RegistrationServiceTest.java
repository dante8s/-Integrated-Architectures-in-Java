package org.example.integrated_architectures.user;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.example.integrated_architectures.sport.Sport;
import org.example.integrated_architectures.sport.SportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test: no Spring context and no database. Repositories are Mockito mocks,
 * the password encoder is real (it is fast and has no dependencies).
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CoachProfileRepository coachProfileRepository;

    @Mock
    SportRepository sportRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(userRepository, coachProfileRepository, sportRepository, passwordEncoder);
    }

    @Test
    void coachIsPendingAndGetsProfileWithSports() {
        Sport tennis = new Sport("Tennis");
        when(userRepository.existsByEmail("coach@test.com")).thenReturn(false);
        when(sportRepository.findAllById(Set.of(1L))).thenReturn(List.of(tennis));

        User user = service.registerCoach(coachForm("  Coach@Test.com "));

        assertThat(user.getEmail()).isEqualTo("coach@test.com");
        assertThat(user.getRole()).isEqualTo(Role.COACH);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);

        // ArgumentCaptor catches the object the service passed to save(...)
        ArgumentCaptor<CoachProfile> profile = ArgumentCaptor.forClass(CoachProfile.class);
        verify(coachProfileRepository).save(profile.capture());
        assertThat(profile.getValue().getUser()).isSameAs(user);
        assertThat(profile.getValue().getSports()).containsExactly(tennis);
        assertThat(profile.getValue().getCity()).isNull(); // blank city -> NULL
    }

    @Test
    void passwordIsStoredAsBcryptHash() {
        when(userRepository.existsByEmail("coach@test.com")).thenReturn(false);

        User user = service.registerCoach(coachForm("coach@test.com"));

        assertThat(user.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", user.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateEmailIsRejected() {
        when(userRepository.existsByEmail("coach@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerCoach(coachForm("coach@test.com")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void studentIsActiveAndHasNoCoachProfile() {
        when(userRepository.existsByEmail("student@test.com")).thenReturn(false);

        User user = service.registerStudent(studentForm("Student@Test.com"));

        assertThat(user.getEmail()).isEqualTo("student@test.com");
        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
        verify(coachProfileRepository, never()).save(any());
    }

    @Test
    void differentPasswordsAreRejected() {
        when(userRepository.existsByEmail("student@test.com")).thenReturn(false);
        StudentRegistrationForm form = studentForm("student@test.com");
        form.setConfirmPassword("another123");

        assertThatThrownBy(() -> service.registerStudent(form))
                .isInstanceOf(PasswordsDoNotMatchException.class);
        verify(userRepository, never()).save(any());
    }

    private StudentRegistrationForm studentForm(String email) {
        StudentRegistrationForm form = new StudentRegistrationForm();
        form.setEmail(email);
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        form.setFirstName("Olena");
        form.setLastName("Shevchenko");
        return form;
    }

    private CoachRegistrationForm coachForm(String email) {
        CoachRegistrationForm form = new CoachRegistrationForm();
        form.setEmail(email);
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        form.setFirstName("Ivan");
        form.setLastName("Petrenko");
        form.setExperienceYears(5);
        form.setHourlyPrice(new BigDecimal("25.00"));
        form.setCity("   ");
        form.setSportIds(Set.of(1L));
        return form;
    }
}
