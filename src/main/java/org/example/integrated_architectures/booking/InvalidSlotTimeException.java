package org.example.integrated_architectures.booking;

public class InvalidSlotTimeException extends RuntimeException {

    public InvalidSlotTimeException(String message) {
        super(message);
    }
}
