package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * Booking life cycle (see BookingStatus):
 * student books a slot -> PENDING -> coach confirms (CONFIRMED) or declines (CANCELLED)
 * -> after the lesson the coach marks it COMPLETED. The student can cancel before the lesson starts.
 * Every method gets the USER id of the logged-in student/coach, so nobody can touch other people's bookings.
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    // Statuses that occupy the slot - the same as the partial unique index uq_bookings_active_slot (V4).
    static final Set<BookingStatus> ACTIVE_STATUSES = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilitySlotRepository slotRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    // ---------- student ----------

    @Transactional
    public Booking book(Long studentUserId, Long slotId) {
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));
        if (!slot.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new SlotNotAvailableException("This slot has already started");
        }
        if (bookingRepository.existsBySlotIdAndStatusIn(slotId, ACTIVE_STATUSES)) {
            throw new SlotNotAvailableException("This slot is already booked");
        }
        // getReferenceById: a proxy with only the id, no SELECT - enough to fill bookings.student_id.
        User student = userRepository.getReferenceById(studentUserId);
        try {
            // Two students can pass the check above at the same moment (race condition).
            // Then the partial unique index in the DB rejects the second INSERT.
            // saveAndFlush sends the INSERT now (not at commit), so the error can be caught here.
            return bookingRepository.saveAndFlush(new Booking(slot, student));
        } catch (DataIntegrityViolationException e) {
            throw new SlotNotAvailableException("This slot is already booked");
        }
    }

    @Transactional
    public void cancel(Long studentUserId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndStudentId(bookingId, studentUserId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        requireStatus(booking, ACTIVE_STATUSES);
        if (!booking.getSlot().getStartsAt().isAfter(LocalDateTime.now())) {
            throw new BookingStatusException("A lesson that has already started cannot be cancelled");
        }
        // No save() needed: the booking is a managed entity (dirty checking).
        booking.setStatus(BookingStatus.CANCELLED);
    }

    // ---------- coach ----------

    @Transactional
    public void confirm(Long coachUserId, Long bookingId) {
        Booking booking = findCoachBooking(coachUserId, bookingId);
        requireStatus(booking, EnumSet.of(BookingStatus.PENDING));
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    // CANCELLED is not an active status, so the slot becomes free for other students again.
    @Transactional
    public void decline(Long coachUserId, Long bookingId) {
        Booking booking = findCoachBooking(coachUserId, bookingId);
        requireStatus(booking, EnumSet.of(BookingStatus.PENDING));
        booking.setStatus(BookingStatus.CANCELLED);
    }

    // Only after the lesson: a review (step 7b) will be allowed only for COMPLETED bookings.
    @Transactional
    public void complete(Long coachUserId, Long bookingId) {
        Booking booking = findCoachBooking(coachUserId, bookingId);
        requireStatus(booking, EnumSet.of(BookingStatus.CONFIRMED));
        if (!booking.getSlot().getEndsAt().isBefore(LocalDateTime.now())) {
            throw new BookingStatusException("The lesson has not ended yet");
        }
        booking.setStatus(BookingStatus.COMPLETED);
    }

    // ---------- helpers ----------

    // A booking of one of this coach's slots; someone else's booking -> 404.
    private Booking findCoachBooking(Long coachUserId, Long bookingId) {
        return bookingRepository.findByIdAndSlotCoachUserId(bookingId, coachUserId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    // Allowed status transitions are checked here, e.g. a CANCELLED booking can't be confirmed.
    private static void requireStatus(Booking booking, Set<BookingStatus> allowed) {
        if (!allowed.contains(booking.getStatus())) {
            throw new BookingStatusException("Not possible for a " + booking.getStatus() + " booking");
        }
    }
}
