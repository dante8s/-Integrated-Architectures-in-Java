package org.example.integrated_architectures.booking;

import jakarta.validation.Valid;
import org.example.integrated_architectures.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * "My slots" page of the logged-in coach: list + "add" form on one page.
 * Access to /coach/** is limited to ROLE_COACH in SecurityConfig.
 * The coach is always taken from the session (@AuthenticationPrincipal), never from the request.
 */
@Controller
@RequestMapping("/coach/slots")
public class CoachSlotController {

    private static final String PAGE = "coach/slots";

    private final SlotService slotService;

    public CoachSlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("form", new SlotForm());
        model.addAttribute("slots", slotService.findUpcoming(me.getId()));
        return PAGE;
    }

    @PostMapping
    public String create(@AuthenticationPrincipal AppUserDetails me,
                         @Valid @ModelAttribute("form") SlotForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                slotService.create(me.getId(), form);
                redirectAttributes.addFlashAttribute("success", "Slot added.");
                return "redirect:/coach/slots";
            } catch (InvalidSlotTimeException | SlotOverlapException e) {
                // reject() without a field name = a "global" error of the whole form (shown above the inputs)
                bindingResult.reject("slot.invalid", e.getMessage());
            }
        }
        // The page is rendered again with the entered values and errors, so it needs the list too.
        model.addAttribute("slots", slotService.findUpcoming(me.getId()));
        return PAGE;
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal AppUserDetails me,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            slotService.delete(me.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Slot deleted.");
        } catch (SlotHasBookingsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/coach/slots";
    }
}
