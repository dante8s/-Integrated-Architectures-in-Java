package org.example.integrated_architectures.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.integrated_architectures.security.WebLogin;
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
    private final WebLogin webLogin;

    public RegistrationController(RegistrationService registrationService, SportService sportService,
                                  WebLogin webLogin) {
        this.registrationService = registrationService;
        this.sportService = sportService;
        this.webLogin = webLogin;
    }

    // ---------- student ----------

    @GetMapping("/student")
    public String studentForm(Model model) {
        model.addAttribute("form", new StudentRegistrationForm());
        return "register-student";
    }

    @PostMapping("/student")
    public String registerStudent(@Valid @ModelAttribute("form") StudentRegistrationForm form,
                                  BindingResult bindingResult,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register-student";
        }

        User student = null;
        try {
            student = registrationService.registerStudent(form);
        } catch (EmailAlreadyUsedException e) {
            bindingResult.rejectValue("email", "email.used", e.getMessage());
        } catch (PasswordsDoNotMatchException e) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", e.getMessage());
        }
        if (bindingResult.hasErrors()) {
            return "register-student";
        }

        // Student is ACTIVE right away: log in automatically and open the personal page.
        webLogin.logIn(student, request, response);
        redirectAttributes.addFlashAttribute("welcome", true);
        return "redirect:/profile";
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

        // 3. Coach is PENDING, so no login: go to the home page, which pops up "waiting for approval".
        //    Post/Redirect/Get: refreshing the page won't submit the form again.
        redirectAttributes.addFlashAttribute("coachPending", true);
        return "redirect:/";
    }

    // The coach form needs the list of sports for checkboxes (also after a failed submit).
    private String showCoachForm(Model model) {
        model.addAttribute("sports", sportService.findAllSorted());
        return "register-coach";
    }
}
