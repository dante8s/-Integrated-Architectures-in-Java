package org.example.integrated_architectures.coach;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus: if a controller doesn't catch it, Spring answers 404 instead of 500.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CoachNotFoundException extends RuntimeException {

    public CoachNotFoundException(Long coachProfileId) {
        super("Coach profile " + coachProfileId + " not found");
    }
}
