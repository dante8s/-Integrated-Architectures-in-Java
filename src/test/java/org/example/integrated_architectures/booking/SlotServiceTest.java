package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of the slot rules: repositories are mocks.
 */
@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    static final Long COACH_USER_ID = 10L;

    @Mock
    AvailabilitySlotRepository slotRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    CoachProfileRepository coachProfileRepository;

    SlotService service;

    @BeforeEach
    void setUp() {
        service = new SlotService(slotRepository, bookingRepository, coachProfileRepository);
    }

    @Test
    void slotInThePastIsRejected() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        assertThatThrownBy(() -> service.create(COACH_USER_ID, form(yesterday, yesterday.plusHours(1))))
                .isInstanceOf(InvalidSlotTimeException.class);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void overlappingSlotIsRejected() {
        // mock(): we only need "some" profile, its fields don't matter here
        CoachProfile coach = mock(CoachProfile.class);
        when(coach.getId()).thenReturn(1L);
        when(coachProfileRepository.findByUserId(COACH_USER_ID)).thenReturn(Optional.of(coach));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        when(slotRepository.existsOverlapping(1L, start, start.plusHours(1))).thenReturn(true);

        assertThatThrownBy(() -> service.create(COACH_USER_ID, form(start, start.plusHours(1))))
                .isInstanceOf(SlotOverlapException.class);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void slotWithBookingsCannotBeDeleted() {
        AvailabilitySlot slot = mock(AvailabilitySlot.class);
        when(slotRepository.findByIdAndCoachUserId(5L, COACH_USER_ID)).thenReturn(Optional.of(slot));
        when(bookingRepository.existsBySlotId(5L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(COACH_USER_ID, 5L))
                .isInstanceOf(SlotHasBookingsException.class);
        verify(slotRepository, never()).delete(any());
    }

    private static SlotForm form(LocalDateTime startsAt, LocalDateTime endsAt) {
        SlotForm form = new SlotForm();
        form.setStartsAt(startsAt);
        form.setEndsAt(endsAt);
        return form;
    }
}
