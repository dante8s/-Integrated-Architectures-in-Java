package org.example.integrated_architectures.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.integrated_architectures.user.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Logs a user in programmatically (without the login form), e.g. right after registration.
 * Session-based, so it belongs to the Thymeleaf layer; the REST API will return a JWT instead.
 */
@Component
public class WebLogin {

    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public void logIn(User user, HttpServletRequest request, HttpServletResponse response) {
        AppUserDetails principal = new AppUserDetails(user);
        // The password was just set by the user, so there is nothing to check: create an authenticated token directly.
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());

        // New session id after login (protection against session fixation), like the form login does.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        SecurityContext context = contextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        contextHolder.setContext(context);
        // Since Spring Security 6 the context is not saved automatically: store it in the HTTP session,
        // so the next requests are logged in too.
        contextRepository.saveContext(context, request, response);
    }
}
