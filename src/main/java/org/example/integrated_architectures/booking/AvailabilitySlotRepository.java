package org.example.integrated_architectures.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    // "My slots" page: WHERE coach.user.id = ? AND startsAt > ? ORDER BY startsAt.
    // By user id, because the logged-in coach is known by AppUserDetails.getId() (a user id).
    List<AvailabilitySlot> findByCoachUserIdAndStartsAtAfterOrderByStartsAtAsc(Long userId, LocalDateTime now);

    /**
     * Two time intervals overlap when each one starts before the other one ends.
     * Touching slots (10:00-11:00 and 11:00-12:00) do NOT overlap: "<" and ">" are strict.
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM AvailabilitySlot s
            WHERE s.coach.id = :coachId
              AND s.startsAt < :endsAt
              AND s.endsAt > :startsAt
            """)
    boolean existsOverlapping(@Param("coachId") Long coachId,
                              @Param("startsAt") LocalDateTime startsAt,
                              @Param("endsAt") LocalDateTime endsAt);

    // WHERE id = ? AND coach.user.id = ? - another coach's slot is "not found" (used by delete).
    Optional<AvailabilitySlot> findByIdAndCoachUserId(Long id, Long userId);

    /**
     * Slots a student can book: in the future and without an active (PENDING/CONFIRMED) booking.
     * NOT EXISTS: a slot with only CANCELLED bookings is free again.
     * By coach PROFILE id, because the public page /coaches/{id}/slots uses the profile id.
     */
    @Query("""
            SELECT s FROM AvailabilitySlot s
            WHERE s.coach.id = :coachId
              AND s.startsAt > :now
              AND NOT EXISTS (
                  SELECT b FROM Booking b
                  WHERE b.slot = s AND b.status IN :activeStatuses)
            ORDER BY s.startsAt
            """)
    List<AvailabilitySlot> findFree(@Param("coachId") Long coachId,
                                    @Param("now") LocalDateTime now,
                                    @Param("activeStatuses") Collection<BookingStatus> activeStatuses);
}
