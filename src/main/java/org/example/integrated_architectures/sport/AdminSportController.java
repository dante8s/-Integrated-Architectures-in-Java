package org.example.integrated_architectures.sport;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin CRUD of sports. Access to /admin/** is limited to ROLE_ADMIN in SecurityConfig.
 * HTML forms only know GET and POST, so update and delete are POST too.
 */
@Controller
@RequestMapping("/admin/sports")
public class AdminSportController {

    private final SportService sportService;

    public AdminSportController(SportService sportService) {
        this.sportService = sportService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("sports", sportService.findAllSorted());
        return "admin/sports";
    }

    // ---------- create ----------

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new SportForm());
        return "admin/sport-form";   // no "sportId" in the model -> the template shows the "create" variant
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") SportForm form, BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/sport-form";
        }
        try {
            sportService.create(form);
        } catch (SportNameAlreadyUsedException e) {
            bindingResult.rejectValue("name", "name.used", e.getMessage());
            return "admin/sport-form";
        }
        redirectAttributes.addFlashAttribute("success", "Sport created.");
        return "redirect:/admin/sports";
    }

    // ---------- edit ----------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", new SportForm(sportService.findById(id)));
        model.addAttribute("sportId", id);   // -> the template shows the "edit" variant
        return "admin/sport-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") SportForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Needed on every return of the form, otherwise it would turn into "create".
        model.addAttribute("sportId", id);
        if (bindingResult.hasErrors()) {
            return "admin/sport-form";
        }
        try {
            sportService.update(id, form);
        } catch (SportNameAlreadyUsedException e) {
            bindingResult.rejectValue("name", "name.used", e.getMessage());
            return "admin/sport-form";
        }
        redirectAttributes.addFlashAttribute("success", "Sport updated.");
        return "redirect:/admin/sports";
    }

    // ---------- delete ----------

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            sportService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Sport deleted.");
        } catch (SportInUseException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sports";
    }
}
