package org.example.integrated_architectures.coach;

import org.example.integrated_architectures.user.Emails;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Admin decides whether a registered coach may log in.
 * PENDING -> ACTIVE (approve) or PENDING -> REJECTED with a reason (reject).
 */
@Service
@Transactional
public class CoachApprovalService {

    private final CoachProfileRepository coachProfileRepository;

    public CoachApprovalService(CoachProfileRepository coachProfileRepository) {
        this.coachProfileRepository = coachProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<CoachProfile> findPending() {
        return coachProfileRepository.findByUserStatusOrderByUserCreatedAtAsc(UserStatus.PENDING);
    }

    /**
     * Shown on the login page to a rejected coach.
     * Called only after the password was checked (see SecurityConfig.authenticationProvider),
     * so a stranger who knows the email cannot read the reason.
     */
    @Transactional(readOnly = true)
    public Optional<String> findRejectionReason(String email) {
        return coachProfileRepository.findByUserEmail(Emails.normalize(email))
                .map(CoachProfile::getRejectionReason);
    }

    /**
     * No save() call is needed: the profile and its user are managed entities,
     * Hibernate writes the changed status to the DB when the transaction commits (dirty checking).
     */
    public void approve(Long coachProfileId) {
        CoachProfile profile = findPendingProfile(coachProfileId);
        profile.getUser().setStatus(UserStatus.ACTIVE);
        profile.setRejectionReason(null);
    }

    public void reject(Long coachProfileId, String reason) {
        // Checked here, not only in HTML: the REST API will call this method too.
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        CoachProfile profile = findPendingProfile(coachProfileId);
        profile.getUser().setStatus(UserStatus.REJECTED);
        profile.setRejectionReason(reason.trim());
    }

    // Only a PENDING coach can be approved or rejected (e.g. protects against a double click).
    private CoachProfile findPendingProfile(Long coachProfileId) {
        CoachProfile profile = coachProfileRepository.findById(coachProfileId)
                .orElseThrow(() -> new CoachNotFoundException(coachProfileId));
        User user = profile.getUser();
        if (user.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("Coach " + user.getEmail() + " is not waiting for approval");
        }
        return profile;
    }
}
