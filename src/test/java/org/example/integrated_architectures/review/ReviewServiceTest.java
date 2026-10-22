package org.example.integrated_architectures.review;

import org.example.integrated_architectures.booking.Booking;
import org.example.integrated_architectures.booking.BookingRepository;
import org.example.integrated_architectures.booking.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test of the review rules. The booking is a real object without slot/student:
 * the rules only look at its status.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    static final Long STUDENT_USER_ID = 20L;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    BookingRepository bookingRepository;

    ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, bookingRepository);
    }

    // A lesson that hasn't happened yet (CONFIRMED) can't be reviewed.
    @Test
    void reviewOnlyAfterCompletedLesson() {
        Booking booking = new Booking(null, null);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByIdAndStudentId(7L, STUDENT_USER_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> service.create(STUDENT_USER_ID, 7L, form(5)))
                .isInstanceOf(ReviewNotAllowedException.class);
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    // One lesson - one review.
    @Test
    void secondReviewOfSameLessonIsRejected() {
        Booking booking = new Booking(null, null);
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdAndStudentId(7L, STUDENT_USER_ID)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(7L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(STUDENT_USER_ID, 7L, form(5)))
                .isInstanceOf(ReviewNotAllowedException.class);
        verify(reviewRepository, never()).saveAndFlush(any());
    }

    private static ReviewForm form(int rating) {
        ReviewForm form = new ReviewForm();
        form.setRating(rating);
        return form;
    }
}
