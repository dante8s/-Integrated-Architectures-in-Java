package org.example.integrated_architectures.booking;

// The slot can't be booked: it is already taken or has already started.
public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(String message) {
        super(message);
    }
}
