package org.example.integrated_architectures.review;

import java.util.List;

/**
 * All reviews of a coach + the average rating (0 when there are no reviews; the page shows "no reviews" then).
 */
public record CoachReviews(List<ReviewView> reviews, double averageRating) {
}
