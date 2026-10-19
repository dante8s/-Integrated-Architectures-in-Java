package org.example.integrated_architectures.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Any status counts: bookings.slot_id is a foreign key, so the DB refuses to delete a slot
    // that has even a CANCELLED booking.
    boolean existsBySlotId(Long slotId);

    // WHERE slot.id = ? AND status IN (...) - "is the slot taken?"
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<BookingStatus> statuses);

    // Student's own booking: another student's booking is "not found".
    Optional<Booking> findByIdAndStudentId(Long id, Long studentUserId);

    // Booking of one of the coach's slots: WHERE id = ? AND slot.coach.user.id = ?
    Optional<Booking> findByIdAndSlotCoachUserId(Long id, Long coachUserId);
}
