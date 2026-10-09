package org.example.integrated_architectures.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.integrated_architectures.coach.CoachApprovalService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import java.io.IOException;
import java.util.Map;

/**
 * Sends the user back to /login with a different parameter for each reason,
 * so login.html can show a specific message.
 * For a rejected coach it also passes the admin's rejection reason as a flash attribute.
 */
@Component
public class LoginFailureHandler extends ExceptionMappingAuthenticationFailureHandler {

    private final CoachApprovalService coachApprovalService;

    // The same mechanism as RedirectAttributes.addFlashAttribute() in controllers.
    // We are in a security filter (before the DispatcherServlet), so we call it directly.
    private final FlashMapManager flashMapManager = new SessionFlashMapManager();

    public LoginFailureHandler(CoachApprovalService coachApprovalService) {
        this.coachApprovalService = coachApprovalService;
        setExceptionMappings(Map.of(
                DisabledException.class.getName(), "/login?pending",
                LockedException.class.getName(), "/login?rejected"));
        setDefaultFailureUrl("/login?error");   // wrong email or password
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof LockedException) {
            String email = request.getParameter("email");
            coachApprovalService.findRejectionReason(email).ifPresent(reason -> {
                FlashMap flashMap = new FlashMap();
                flashMap.put("rejectionReason", reason);
                flashMapManager.saveOutputFlashMap(flashMap, request, response);
            });
        }
        // The parent class does the redirect according to the mappings above.
        super.onAuthenticationFailure(request, response, exception);
    }
}
