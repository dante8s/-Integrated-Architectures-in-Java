package org.example.integrated_architectures.user;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.example.integrated_architectures.sport.SportRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Registration of students and coaches. Used by the Thymeleaf controller now
 * and by the REST API later, so all rules live here, not in controllers.
 */
@Service
@Transactional
public class RegistrationService {

    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final SportRepository sportRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository,
                               CoachProfileRepository coachProfileRepository,
                               SportRepository sportRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.coachProfileRepository = coachProfileRepository;
        this.sportRepository = sportRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Student can log in right away, so the status is ACTIVE.
     */
    public User registerStudent(StudentRegistrationForm form) {
        String email = normalizeEmail(form.getEmail());
        ensureEmailIsFree(email);
        ensurePasswordsMatch(form.getPassword(), form.getConfirmPassword());

        User user = new User(
                email,
                passwordEncoder.encode(form.getPassword()),
                form.getFirstName().trim(),
                form.getLastName().trim(),
                Role.STUDENT,
                UserStatus.ACTIVE);
        userRepository.save(user);
        return user;
    }

    /**
     * Coach waits for admin approval (PENDING) and gets a CoachProfile with the selected sports.
     * Both rows are saved in one transaction: if the profile fails, the user is not saved either.
     */
    public User registerCoach(CoachRegistrationForm form) {
        String email = normalizeEmail(form.getEmail());
        ensureEmailIsFree(email);
        ensurePasswordsMatch(form.getPassword(), form.getConfirmPassword());

        User user = new User(
                email,
                passwordEncoder.encode(form.getPassword()),
                form.getFirstName().trim(),
                form.getLastName().trim(),
                Role.COACH,
                UserStatus.PENDING);
        userRepository.save(user);

        CoachProfile profile = new CoachProfile(user, form.getExperienceYears(), form.getHourlyPrice());
        profile.setCity(blankToNull(form.getCity()));
        profile.setOnline(form.isOnline());
        profile.setBio(blankToNull(form.getBio()));
        profile.getSports().addAll(sportRepository.findAllById(form.getSportIds()));
        coachProfileRepository.save(profile);

        return user;
    }

    // "  Ivan@Mail.COM " and "ivan@mail.com" must be the same account.
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureEmailIsFree(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
    }

    private void ensurePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException();
        }
    }

    // Store NULL instead of an empty string for optional text fields.
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
