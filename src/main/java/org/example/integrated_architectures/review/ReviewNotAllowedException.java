package org.example.integrated_architectures.review;

// The lesson can't be reviewed: it is not completed yet, or it already has a review.
public class ReviewNotAllowedException extends RuntimeException {

    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
