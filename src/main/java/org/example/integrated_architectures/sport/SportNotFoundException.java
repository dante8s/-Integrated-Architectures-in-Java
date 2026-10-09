package org.example.integrated_architectures.sport;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus: if a controller doesn't catch it, Spring answers 404 instead of 500.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SportNotFoundException extends RuntimeException {

    public SportNotFoundException(Long sportId) {
        super("Sport " + sportId + " not found");
    }
}
