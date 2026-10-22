package org.example.integrated_architectures.review;

import org.example.integrated_architectures.booking.Booking;
import org.example.integrated_architectures.user.User;

import java.time.LocalDateTime;

/**
 * Which lesson the student is reviewing - shown above the review form ("Lesson with ... on ...").
 */
public record ReviewTarget(
        Long bookingId,
        String coachName,
        LocalDateTime lessonDate) {

    // Must be called inside a transaction: reads the lazy slot -> coach -> user.
    static ReviewTarget from(Booking booking) {
        User coachUser = booking.getSlot().getCoach().getUser();
        return new ReviewTarget(
                booking.getId(),
                coachUser.getFirstName() + " " + coachUser.getLastName(),
                booking.getSlot().getStartsAt());
    }
}
