package org.example.integrated_architectures.review;

import org.example.integrated_architectures.booking.Booking;
import org.example.integrated_architectures.booking.BookingNotFoundException;
import org.example.integrated_architectures.booking.BookingRepository;
import org.example.integrated_architectures.booking.BookingStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reviews: a student rates a lesson only after the coach marked it COMPLETED, and only once.
 * The review/ package depends on booking/, never the other way round.
 */
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    // For the form page: checks the rules first, so the student doesn't fill in a form that can't be sent.
    public ReviewTarget getReviewTarget(Long studentUserId, Long bookingId) {
        return ReviewTarget.from(findReviewableBooking(studentUserId, bookingId));
    }

    @Transactional
    public Review create(Long studentUserId, Long bookingId, ReviewForm form) {
        Booking booking = findReviewableBooking(studentUserId, bookingId);
        // An empty textarea sends "" - store NULL instead of an empty string.
        String comment = form.getComment() == null || form.getComment().isBlank() ? null : form.getComment().trim();
        try {
            // Double click: two requests can pass existsByBookingId at once; UNIQUE (booking_id) stops the second.
            return reviewRepository.saveAndFlush(new Review(booking, form.getRating(), comment));
        } catch (DataIntegrityViolationException e) {
            throw new ReviewNotAllowedException("You have already reviewed this lesson");
        }
    }

    // Public reviews page of a coach (coachId = coach PROFILE id).
    public CoachReviews findCoachReviews(Long coachId) {
        List<ReviewView> reviews = reviewRepository.findByBookingSlotCoachIdOrderByCreatedAtDesc(coachId).stream()
                .map(ReviewView::from)
                .toList();
        // average() of an empty stream is "empty" -> 0
        double average = reviews.stream().mapToInt(ReviewView::rating).average().orElse(0);
        return new CoachReviews(reviews, average);
    }

    // Only the student's own booking (another one -> 404), only COMPLETED, only without a review.
    private Booking findReviewableBooking(Long studentUserId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndStudentId(bookingId, studentUserId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ReviewNotAllowedException("You can review a lesson only after it is completed");
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ReviewNotAllowedException("You have already reviewed this lesson");
        }
        return booking;
    }
}
