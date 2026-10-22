package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A coach manages their own availability slots.
 * Every method gets the coach's USER id (AppUserDetails.getId()), never a slot owner from the form,
 * so a coach can't touch another coach's slots.
 */
@Service
@Transactional(readOnly = true)
public class SlotService {

    private final AvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final CoachProfileRepository coachProfileRepository;

    public SlotService(AvailabilitySlotRepository slotRepository,
                       BookingRepository bookingRepository,
                       CoachProfileRepository coachProfileRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.coachProfileRepository = coachProfileRepository;
    }

    // Past slots are not shown: they can't be booked anymore.
    public List<AvailabilitySlot> findUpcoming(Long coachUserId) {
        return slotRepository.findByCoachUserIdAndStartsAtAfterOrderByStartsAtAsc(coachUserId, LocalDateTime.now());
    }

    // For students: future slots of a coach that nobody has booked yet. coachId = coach PROFILE id.
    public List<AvailabilitySlot> findFree(Long coachId) {
        return slotRepository.findFree(coachId, LocalDateTime.now(), BookingService.ACTIVE_STATUSES);
    }

    @Transactional
    public AvailabilitySlot create(Long coachUserId, SlotForm form) {
        LocalDateTime startsAt = form.getStartsAt();
        LocalDateTime endsAt = form.getEndsAt();
        if (!startsAt.isAfter(LocalDateTime.now())) {
            throw new InvalidSlotTimeException("The slot must start in the future");
        }
        // The DB has CHECK (ends_at > starts_at) too, but here the coach gets a clear message.
        if (!endsAt.isAfter(startsAt)) {
            throw new InvalidSlotTimeException("The end must be after the start");
        }
        // A logged-in COACH always has a profile (created at registration), so this is a programming error.
        CoachProfile coach = coachProfileRepository.findByUserId(coachUserId)
                .orElseThrow(() -> new IllegalStateException("No coach profile for user " + coachUserId));
        if (slotRepository.existsOverlapping(coach.getId(), startsAt, endsAt)) {
            throw new SlotOverlapException();
        }
        return slotRepository.save(new AvailabilitySlot(coach, startsAt, endsAt));
    }

    @Transactional
    public void delete(Long coachUserId, Long slotId) {
        // Searching by slot id AND owner: another coach's slot looks exactly like a missing one.
        AvailabilitySlot slot = slotRepository.findByIdAndCoachUserId(slotId, coachUserId)
                .orElseThrow(() -> new SlotNotFoundException(slotId));
        if (bookingRepository.existsBySlotId(slotId)) {
            throw new SlotHasBookingsException(slotId);
        }
        slotRepository.delete(slot);
    }
}
