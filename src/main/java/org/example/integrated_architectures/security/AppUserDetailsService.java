package org.example.integrated_architectures.security;

import org.example.integrated_architectures.user.Emails;
import org.example.integrated_architectures.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security calls this on login to find the user by the typed email.
 * The password check itself is done by Spring with our PasswordEncoder bean.
 */
@Service
@Transactional(readOnly = true)
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmail(Emails.normalize(email))
                .map(AppUserDetails::new)
                // Spring turns this into a generic "bad credentials" error,
                // so the login page does not reveal which emails exist.
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
