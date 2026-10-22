package org.example.integrated_architectures.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The logged-in user edits their own data. The user id always comes from the session.
 */
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileForm getForm(Long userId) {
        return ProfileForm.from(findUser(userId));
    }

    // Returns the updated user: the controller needs it to refresh the logged-in user in the session.
    @Transactional
    public User updateName(Long userId, ProfileForm form) {
        User user = findUser(userId);
        user.rename(form.getFirstName().trim(), form.getLastName().trim());   // dirty checking, no save()
        return user;
    }

    // The logged-in user exists (they just passed the login), so a missing one is a programming error.
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("No user " + userId));
    }
}
