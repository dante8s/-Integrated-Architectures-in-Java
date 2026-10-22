package org.example.integrated_architectures.coach;

import jakarta.validation.Valid;
import org.example.integrated_architectures.security.AppUserDetails;
import org.example.integrated_architectures.sport.SportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * "My profile" of the logged-in coach. /coach/** is ROLE_COACH only (SecurityConfig).
 */
@Controller
@RequestMapping("/coach/profile")
public class CoachProfileController {

    private static final String PAGE = "coach/profile";

    private final CoachProfileService coachProfileService;
    private final SportService sportService;

    public CoachProfileController(CoachProfileService coachProfileService, SportService sportService) {
        this.coachProfileService = coachProfileService;
        this.sportService = sportService;
    }

    @GetMapping
    public String form(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("form", coachProfileService.getForm(me.getId()));
        model.addAttribute("sports", sportService.findAllSorted());
        return PAGE;
    }

    @PostMapping
    public String update(@AuthenticationPrincipal AppUserDetails me,
                         @Valid @ModelAttribute("form") CoachProfileForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // The page is shown again with the entered values and errors; the checkboxes need the sports.
            model.addAttribute("sports", sportService.findAllSorted());
            return PAGE;
        }
        coachProfileService.update(me.getId(), form);
        redirectAttributes.addFlashAttribute("success", "Profile saved.");
        return "redirect:/coach/profile";
    }
}
