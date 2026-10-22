package org.example.integrated_architectures.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * "Leave a review" form. The booking id is in the URL, never in the form fields.
 * Integer (not int): an empty value becomes null, and @NotNull shows a clear message.
 */
public class ReviewForm {

    @NotNull(message = "Choose a rating")
    @Min(value = 1, message = "Rating must be from 1 to 5")
    @Max(value = 5, message = "Rating must be from 1 to 5")
    private Integer rating;

    @Size(max = 1000, message = "At most 1000 characters")
    private String comment;

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
