package org.example.integrated_architectures.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.integrated_architectures.security.AppUserDetails;
import org.example.integrated_architectures.security.WebLogin;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Personal page of the logged-in user (any role): account info + editing the name.
 */
@Controller
public class ProfileController {

    private static final String PAGE = "profile";

    private final ProfileService profileService;
    private final WebLogin webLogin;

    public ProfileController(ProfileService profileService, WebLogin webLogin) {
        this.profileService = profileService;
        this.webLogin = webLogin;
    }

    // @AuthenticationPrincipal gives the AppUserDetails of the current user from the session.
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("me", me);
        model.addAttribute("form", profileService.getForm(me.getId()));
        return PAGE;
    }

    @PostMapping("/profile")
    public String update(@AuthenticationPrincipal AppUserDetails me,
                         @Valid @ModelAttribute("form") ProfileForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes,
                         HttpServletRequest request, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("me", me);
            return PAGE;
        }
        User user = profileService.updateName(me.getId(), form);
        webLogin.refresh(user, request, response);   // the navbar shows the new name at once
        redirectAttributes.addFlashAttribute("success", "Profile saved.");
        return "redirect:/profile";
    }
}
