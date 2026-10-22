package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.coach.CoachProfile;

import java.time.LocalDateTime;

/**
 * One row of the student's "My bookings" page (a DTO, like CoachView).
 * cancellable is decided by BookingService, so the template has no business rules,
 * only "show the Cancel button or not". The REST API (step 9) can return the same object.
 */
public record StudentBookingView(
        Long id,
        Long coachId,
        String coachName,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BookingStatus status,
        boolean cancellable) {

    // Needs slot -> coach -> user loaded (see BookingRepository.findByStudentIdOrderBySlotStartsAtDesc).
    static StudentBookingView from(Booking booking, boolean cancellable) {
        AvailabilitySlot slot = booking.getSlot();
        CoachProfile coach = slot.getCoach();
        return new StudentBookingView(
                booking.getId(),
                coach.getId(),
                coach.getUser().getFirstName() + " " + coach.getUser().getLastName(),
                slot.getStartsAt(),
                slot.getEndsAt(),
                booking.getStatus(),
                cancellable);
    }
}
