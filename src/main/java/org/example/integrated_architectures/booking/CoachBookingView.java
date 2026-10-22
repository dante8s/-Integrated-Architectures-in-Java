package org.example.integrated_architectures.booking;

import java.time.LocalDateTime;

/**
 * One row of the coach's "Booking requests" page (a DTO, like StudentBookingView).
 * The flags are decided by BookingService, the template only shows or hides the buttons.
 * canAnswer = the coach can confirm or decline (PENDING), canComplete = mark the lesson as done.
 */
public record CoachBookingView(
        Long id,
        String studentName,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BookingStatus status,
        boolean canAnswer,
        boolean canComplete) {

    // Needs slot and student loaded (see BookingRepository.findBySlotCoachUserIdOrderBySlotStartsAtDesc).
    static CoachBookingView from(Booking booking, boolean canAnswer, boolean canComplete) {
        return new CoachBookingView(
                booking.getId(),
                booking.getStudent().getFirstName() + " " + booking.getStudent().getLastName(),
                booking.getSlot().getStartsAt(),
                booking.getSlot().getEndsAt(),
                booking.getStatus(),
                canAnswer,
                canComplete);
    }
}
