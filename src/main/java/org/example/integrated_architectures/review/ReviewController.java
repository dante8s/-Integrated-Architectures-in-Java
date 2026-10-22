package org.example.integrated_architectures.review;

import jakarta.validation.Valid;
import org.example.integrated_architectures.coach.CoachCatalogService;
import org.example.integrated_architectures.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * - GET /coaches/{coachId}/reviews: public reviews page (a separate page for the same reason as the free slots:
 *   coach/ must not depend on review/).
 * - /student/bookings/{bookingId}/review: the review form, ROLE_STUDENT only (SecurityConfig "/student/**").
 */
@Controller
public class ReviewController {

    private static final String FORM = "review/form";
    private static final String MY_BOOKINGS = "redirect:/student/bookings";

    private final ReviewService reviewService;
    private final CoachCatalogService coachCatalogService;

    public ReviewController(ReviewService reviewService, CoachCatalogService coachCatalogService) {
        this.reviewService = reviewService;
        this.coachCatalogService = coachCatalogService;
    }

    @GetMapping("/coaches/{coachId}/reviews")
    public String coachReviews(@PathVariable Long coachId, Model model) {
        model.addAttribute("coach", coachCatalogService.findActiveCoach(coachId));   // not ACTIVE -> 404
        model.addAttribute("coachReviews", reviewService.findCoachReviews(coachId));
        return "review/coach-reviews";
    }

    @GetMapping("/student/bookings/{bookingId}/review")
    public String form(@AuthenticationPrincipal AppUserDetails me,
                       @PathVariable Long bookingId,
                       Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("target", reviewService.getReviewTarget(me.getId(), bookingId));
            model.addAttribute("form", new ReviewForm());
            return FORM;
        } catch (ReviewNotAllowedException e) {
            // e.g. the student has already reviewed this lesson
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return MY_BOOKINGS;
        }
    }

    @PostMapping("/student/bookings/{bookingId}/review")
    public String create(@AuthenticationPrincipal AppUserDetails me,
                         @PathVariable Long bookingId,
                         @Valid @ModelAttribute("form") ReviewForm form, BindingResult bindingResult,
                         Model model, RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                // The form is shown again with the errors, so it needs the lesson info too.
                model.addAttribute("target", reviewService.getReviewTarget(me.getId(), bookingId));
                return FORM;
            }
            reviewService.create(me.getId(), bookingId, form);
            redirectAttributes.addFlashAttribute("success", "Thank you for your review!");
        } catch (ReviewNotAllowedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return MY_BOOKINGS;
    }
}
