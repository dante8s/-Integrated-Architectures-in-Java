package org.example.integrated_architectures.security;

import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The logged-in user as Spring Security sees it. Stored in the HTTP session,
 * so it keeps plain copied values instead of the JPA entity.
 */
public class AppUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final Role role;
    private final UserStatus status;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.firstName = user.getFirstName();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    // hasRole("ADMIN") checks for the authority "ROLE_ADMIN".
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    // PENDING coach -> false -> Spring throws DisabledException ("waiting for approval").
    @Override
    public boolean isEnabled() {
        return status != UserStatus.PENDING;
    }

    // REJECTED coach -> false -> Spring throws LockedException ("rejected").
    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public Role getRole() {
        return role;
    }
}
