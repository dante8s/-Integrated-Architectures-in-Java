package org.example.integrated_architectures.review;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBookingId(Long bookingId);

    // Reviews of one coach: WHERE booking.slot.coach.id = ? (coach PROFILE id, as in /coaches/{id}).
    // The page shows the student's name and the lesson date, so student and slot are loaded with JOINs.
    @EntityGraph(attributePaths = {"booking", "booking.student", "booking.slot"})
    List<Review> findByBookingSlotCoachIdOrderByCreatedAtDesc(Long coachId);
}
