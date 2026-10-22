package org.example.integrated_architectures.review;

import java.time.LocalDateTime;

/**
 * One review on the public reviews page. Only the student's first name is shown (privacy).
 */
public record ReviewView(
        int rating,
        String comment,
        String studentFirstName,
        LocalDateTime lessonDate) {

    static ReviewView from(Review review) {
        return new ReviewView(
                review.getRating(),
                review.getComment(),
                review.getBooking().getStudent().getFirstName(),
                review.getBooking().getSlot().getStartsAt());
    }
}
