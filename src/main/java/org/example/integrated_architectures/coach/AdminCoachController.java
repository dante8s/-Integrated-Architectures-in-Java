package org.example.integrated_architectures.coach;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin page with coaches waiting for approval.
 * Access to /admin/** is limited to ROLE_ADMIN in SecurityConfig.
 */
@Controller
@RequestMapping("/admin/coaches")
public class AdminCoachController {

    private final CoachApprovalService coachApprovalService;

    public AdminCoachController(CoachApprovalService coachApprovalService) {
        this.coachApprovalService = coachApprovalService;
    }

    @GetMapping
    public String pendingCoaches(Model model) {
        model.addAttribute("coaches", coachApprovalService.findPending());
        return "admin/coaches";
    }

    // POST + redirect (Post/Redirect/Get): refreshing the page won't approve twice.
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            coachApprovalService.approve(id);
            redirectAttributes.addFlashAttribute("success", "Coach approved, they can log in now.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coaches";
    }

    // reason = value of <textarea name="reason"> from the form.
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam String reason,
                         RedirectAttributes redirectAttributes) {
        try {
            coachApprovalService.reject(id, reason);
            redirectAttributes.addFlashAttribute("success", "Coach rejected.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/coaches";
    }
}
