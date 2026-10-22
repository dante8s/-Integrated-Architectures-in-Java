package org.example.integrated_architectures.booking;

import org.example.integrated_architectures.coach.CoachProfile;
import org.example.integrated_architectures.coach.CoachProfileRepository;
import org.example.integrated_architectures.user.Role;
import org.example.integrated_architectures.user.User;
import org.example.integrated_architectures.user.UserRepository;
import org.example.integrated_architectures.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks the partial unique index uq_bookings_active_slot (V4) in the real Postgres.
 * A mock can't test this: the rule lives in the database. The transaction is rolled back after the test.
 */
// Uses the Postgres from compose.yaml for now; will be replaced by Testcontainers.
@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
@Transactional
class BookingRepositoryTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CoachProfileRepository coachProfileRepository;

    @Autowired
    AvailabilitySlotRepository slotRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Test
    void slotCanHaveOnlyOneActiveBooking() {
        User coachUser = userRepository.save(
                new User("coach@test.local", "hash", "Coach", "Test", Role.COACH, UserStatus.ACTIVE));
        CoachProfile coach = coachProfileRepository.save(new CoachProfile(coachUser, 3, new BigDecimal("20.00")));
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        AvailabilitySlot slot = slotRepository.save(new AvailabilitySlot(coach, start, start.plusHours(1)));
        User student1 = saveStudent("student1@test.local");
        User student2 = saveStudent("student2@test.local");
        User student3 = saveStudent("student3@test.local");

        // A cancelled booking doesn't occupy the slot: the index covers only PENDING and CONFIRMED.
        Booking cancelled = new Booking(slot, student1);
        cancelled.setStatus(BookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(cancelled);
        bookingRepository.saveAndFlush(new Booking(slot, student2));   // PENDING - OK

        // The second active booking of the same slot is rejected by the database itself.
        assertThatThrownBy(() -> bookingRepository.saveAndFlush(new Booking(slot, student3)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // The student sees only future slots without an active booking (AvailabilitySlotRepository.findFree).
    @Test
    void onlyFutureNotBookedSlotsAreFree() {
        User coachUser = userRepository.save(
                new User("coach@test.local", "hash", "Coach", "Test", Role.COACH, UserStatus.ACTIVE));
        CoachProfile coach = coachProfileRepository.save(new CoachProfile(coachUser, 3, new BigDecimal("20.00")));
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        AvailabilitySlot free = slotRepository.save(new AvailabilitySlot(coach, tomorrow, tomorrow.plusHours(1)));
        AvailabilitySlot booked = slotRepository.save(
                new AvailabilitySlot(coach, tomorrow.plusHours(2), tomorrow.plusHours(3)));
        AvailabilitySlot freedAgain = slotRepository.save(
                new AvailabilitySlot(coach, tomorrow.plusHours(4), tomorrow.plusHours(5)));
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        slotRepository.save(new AvailabilitySlot(coach, yesterday, yesterday.plusHours(1)));   // past

        User student = saveStudent("student@test.local");
        bookingRepository.save(new Booking(booked, student));                                   // PENDING
        Booking cancelled = new Booking(freedAgain, student);
        cancelled.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(cancelled);

        assertThat(slotRepository.findFree(coach.getId(), LocalDateTime.now(), BookingService.ACTIVE_STATUSES))
                .containsExactly(free, freedAgain);
    }

    private User saveStudent(String email) {
        return userRepository.save(new User(email, "hash", "Student", "Test", Role.STUDENT, UserStatus.ACTIVE));
    }
}
