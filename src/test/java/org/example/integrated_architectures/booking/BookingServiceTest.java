package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of the booking rules: repositories are mocks, slots and bookings are real objects
 * (coach and student are not needed for these rules, so they are null).
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    static final Long STUDENT_USER_ID = 20L;
    static final Long COACH_USER_ID = 10L;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    AvailabilitySlotRepository slotRepository;

    @Mock
    UserRepository userRepository;

    BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService(bookingRepository, slotRepository, userRepository);
    }

    @Test
    void pastSlotCannotBeBooked() {
        when(slotRepository.findById(5L)).thenReturn(Optional.of(slotStartingAt(LocalDateTime.now().minusHours(2))));

        assertThatThrownBy(() -> service.book(STUDENT_USER_ID, 5L))
                .isInstanceOf(SlotNotAvailableException.class);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void bookedSlotCannotBeBookedAgain() {
        when(slotRepository.findById(5L)).thenReturn(Optional.of(slotStartingAt(LocalDateTime.now().plusDays(1))));
        when(bookingRepository.existsBySlotIdAndStatusIn(5L, BookingService.ACTIVE_STATUSES)).thenReturn(true);

        assertThatThrownBy(() -> service.book(STUDENT_USER_ID, 5L))
                .isInstanceOf(SlotNotAvailableException.class);
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelledBookingCannotBeConfirmed() {
        Booking booking = new Booking(slotStartingAt(LocalDateTime.now().plusDays(1)), null);
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdAndSlotCoachUserId(7L, COACH_USER_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.confirm(COACH_USER_ID, 7L))
                .isInstanceOf(BookingStatusException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void bookingCannotBeCompletedBeforeLessonEnds() {
        Booking booking = new Booking(slotStartingAt(LocalDateTime.now().plusDays(1)), null);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdAndSlotCoachUserId(7L, COACH_USER_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.complete(COACH_USER_ID, 7L))
                .isInstanceOf(BookingStatusException.class);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // A one-hour slot; the coach is not needed for the booking rules.
    private static AvailabilitySlot slotStartingAt(LocalDateTime startsAt) {
        return new AvailabilitySlot(null, startsAt, startsAt.plusHours(1));
    }
}
