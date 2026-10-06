package org.example.integrated_architectures.user;

import org.example.integrated_architectures.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Personal page of the logged-in user. Later: bookings (step 7).
 */
@Controller
public class ProfileController {

    // @AuthenticationPrincipal gives the AppUserDetails of the current user from the session.
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("me", me);
        return "profile";
    }
}
