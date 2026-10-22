package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.coach.CoachCatalogService;
import org.example.integrated_architectures.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Booking pages for students.
 * - GET /coaches/{coachId}/slots is public (SecurityConfig): guests see free time, but must log in to book.
 * - /student/** is for ROLE_STUDENT only.
 * The free slots live on their own page, not on /coaches/{id}: otherwise the coach/ package would
 * depend on booking/, while booking/ already depends on coach/ (a cycle).
 */
@Controller
public class StudentBookingController {

    private final SlotService slotService;
    private final BookingService bookingService;
    private final CoachCatalogService coachCatalogService;

    public StudentBookingController(SlotService slotService,
                                    BookingService bookingService,
                                    CoachCatalogService coachCatalogService) {
        this.slotService = slotService;
        this.bookingService = bookingService;
        this.coachCatalogService = coachCatalogService;
    }

    // findActiveCoach: a PENDING/REJECTED/unknown coach -> 404, the same as on the coach page.
    @GetMapping("/coaches/{coachId}/slots")
    public String freeSlots(@PathVariable Long coachId, Model model) {
        model.addAttribute("coach", coachCatalogService.findActiveCoach(coachId));
        model.addAttribute("slots", slotService.findFree(coachId));
        return "booking/free-slots";
    }

    // coachId is used only to come back to the same page after an error, the booking itself needs slotId.
    @PostMapping("/student/bookings")
    public String book(@AuthenticationPrincipal AppUserDetails me,
                       @RequestParam Long slotId,
                       @RequestParam Long coachId,
                       RedirectAttributes redirectAttributes) {
        try {
            bookingService.book(me.getId(), slotId);
            redirectAttributes.addFlashAttribute("success", "Booked! Wait for the coach to confirm.");
            return "redirect:/student/bookings";
        } catch (SlotNotAvailableException e) {
            // e.g. another student booked this slot a second earlier
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/coaches/" + coachId + "/slots";
        }
    }

    @GetMapping("/student/bookings")
    public String myBookings(@AuthenticationPrincipal AppUserDetails me, Model model) {
        model.addAttribute("bookings", bookingService.findStudentBookings(me.getId()));
        return "student/bookings";
    }

    // Someone else's booking -> BookingNotFoundException -> 404 (its @ResponseStatus).
    @PostMapping("/student/bookings/{id}/cancel")
    public String cancel(@AuthenticationPrincipal AppUserDetails me,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancel(me.getId(), id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled.");
        } catch (BookingStatusException e) {
            // e.g. the lesson has already started, or the coach declined it a moment ago
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/bookings";
    }
}
