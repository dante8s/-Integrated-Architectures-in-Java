package org.example.integrated_architectures.user;

import jakarta.validation.Valid;
import org.example.integrated_architectures.sport.SportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Thin adapter between HTML forms and RegistrationService.
 */
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final SportService sportService;

    public RegistrationController(RegistrationService registrationService, SportService sportService) {
        this.registrationService = registrationService;
        this.sportService = sportService;
    }

    // ---------- student ----------

    @GetMapping("/student")
    public String studentForm(Model model) {
        model.addAttribute("form", new StudentRegistrationForm());
        return "register-student";
    }

    @PostMapping("/student")
    public String registerStudent(@Valid @ModelAttribute("form") StudentRegistrationForm form,
                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register-student";
        }

        try {
            registrationService.registerStudent(form);
        } catch (EmailAlreadyUsedException e) {
            bindingResult.rejectValue("email", "email.used", e.getMessage());
        } catch (PasswordsDoNotMatchException e) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            return "register-student";
        }
        // No flash attribute: register-success shows the student message by default
        return "redirect:/register/success";
    }

    // ---------- coach ----------

    @GetMapping("/coach")
    public String coachForm(Model model) {
        model.addAttribute("form", new CoachRegistrationForm());
        return showCoachForm(model);
    }

    @PostMapping("/coach")
    public String registerCoach(@Valid @ModelAttribute("form") CoachRegistrationForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // 1. Format errors from Bean Validation (@NotBlank, @Email, ...)
        if (bindingResult.hasErrors()) {
            return showCoachForm(model);
        }

        // 2. Business errors from the service are shown next to the related field
        try {
            registrationService.registerCoach(form);
        } catch (EmailAlreadyUsedException e) {
            bindingResult.rejectValue("email", "email.used", e.getMessage());
        } catch (PasswordsDoNotMatchException e) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            return showCoachForm(model);
        }

        // 3. Post/Redirect/Get: refreshing the success page won't submit the form again
        redirectAttributes.addFlashAttribute("registeredAsCoach", true);
        return "redirect:/register/success";
    }

    // ---------- result ----------

    @GetMapping("/success")
    public String success() {
        return "register-success";
    }

    // The coach form needs the list of sports for checkboxes (also after a failed submit).
    private String showCoachForm(Model model) {
        model.addAttribute("sports", sportService.findAllSorted());
        return "register-coach";
    }
}
