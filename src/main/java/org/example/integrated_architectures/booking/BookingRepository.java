package org.example.integrated_architectures.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
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

    // "My bookings" of a student, newest lessons first.
    // @EntityGraph loads slot -> coach -> user in the same SELECT (JOINs): the view needs the coach's name,
    // and with open-in-view=false lazy fields can't be loaded later. Also avoids N+1 queries.
    @EntityGraph(attributePaths = {"slot", "slot.coach", "slot.coach.user"})
    List<Booking> findByStudentIdOrderBySlotStartsAtDesc(Long studentUserId);

    // "Booking requests" of a coach: bookings of all their slots, with the student's name for the page.
    @EntityGraph(attributePaths = {"slot", "student"})
    List<Booking> findBySlotCoachUserIdOrderBySlotStartsAtDesc(Long coachUserId);
}
