package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.sport.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A coach edits their own profile. The coach is always the logged-in user (coach USER id from the session),
 * so another coach's profile can't be changed. No new admin approval: changes are visible in the catalog at once.
 */
@Service
@Transactional(readOnly = true)
public class CoachProfileService {

    private final CoachProfileRepository coachProfileRepository;
    private final SportRepository sportRepository;

    public CoachProfileService(CoachProfileRepository coachProfileRepository, SportRepository sportRepository) {
        this.coachProfileRepository = coachProfileRepository;
        this.sportRepository = sportRepository;
    }

    public CoachProfileForm getForm(Long coachUserId) {
        return CoachProfileForm.from(findProfile(coachUserId));
    }

    @Transactional
    public void update(Long coachUserId, CoachProfileForm form) {
        CoachProfile profile = findProfile(coachUserId);
        profile.setExperienceYears(form.getExperienceYears());
        profile.setHourlyPrice(form.getHourlyPrice());
        profile.setCity(blankToNull(form.getCity()));
        profile.setBio(blankToNull(form.getBio()));
        // Replace the whole set: Hibernate deletes the old rows of coach_profile_sports and inserts the new ones.
        profile.getSports().clear();
        profile.getSports().addAll(sportRepository.findAllById(form.getSportIds()));
        // No save(): the profile is a managed entity, changes are written at commit (dirty checking).
    }

    // A logged-in COACH always has a profile (created at registration), so this is a programming error.
    private CoachProfile findProfile(Long coachUserId) {
        return coachProfileRepository.findByUserId(coachUserId)
                .orElseThrow(() -> new IllegalStateException("No coach profile for user " + coachUserId));
    }

    // Store NULL instead of an empty string for optional text fields (as in RegistrationService).
    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
