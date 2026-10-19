package org.example.integrated_architectures.booking;

// The action is not allowed in the booking's current status (e.g. confirming a cancelled booking).
public class BookingStatusException extends RuntimeException {

    public BookingStatusException(String message) {
        super(message);
    }
}
