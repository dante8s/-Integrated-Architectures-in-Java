package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * "Booking requests" page of the logged-in coach: confirm / decline / complete.
 * /coach/** is ROLE_COACH only (SecurityConfig). Another coach's booking -> BookingNotFoundException -> 404.
 */
@Controller
@RequestMapping("/coach/bookings")
public class CoachBookingController {

    private static final String REDIRECT = "redirect:/coach/bookings";

    private final BookingService bookingService;

    public CoachBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("bookings", bookingService.findCoachBookings(me.getId()));
        return "coach/bookings";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@AuthenticationPrincipal AppUserDetails me,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirm(me.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Booking confirmed.");
        } catch (BookingStatusException e) {
            // e.g. the student cancelled it while the coach had the page open
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT;
    }

    @PostMapping("/{id}/decline")
    public String decline(@AuthenticationPrincipal AppUserDetails me,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            bookingService.decline(me.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Booking declined.");
        } catch (BookingStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT;
    }

    // BookingStatusException also when the lesson has not ended yet (see BookingService.complete).
    @PostMapping("/{id}/complete")
    public String complete(@AuthenticationPrincipal AppUserDetails me,
                           @PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            bookingService.complete(me.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Lesson marked as completed.");
        } catch (BookingStatusException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return REDIRECT;
    }
}
