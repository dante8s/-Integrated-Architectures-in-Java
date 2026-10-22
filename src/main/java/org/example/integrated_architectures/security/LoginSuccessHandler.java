package org.example.integrated_architectures.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Where to go after a successful login.
 * "SavedRequestAware": if a guest opened a protected page (e.g. /student/bookings) and was sent to /login,
 * they come back to that page. Only when there is no such page, determineTargetUrl() picks a page by role.
 */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        AppUserDetails me = (AppUserDetails) authentication.getPrincipal();
        // switch expression over the enum: the compiler checks that every role is covered
        return switch (me.getRole()) {
            case STUDENT -> "/profile";
            case COACH -> "/coach/profile";
            case ADMIN -> "/admin/coaches";
        };
    }
}
