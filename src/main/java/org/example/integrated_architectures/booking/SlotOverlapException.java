package org.example.integrated_architectures.booking;

public class SlotOverlapException extends RuntimeException {

    public SlotOverlapException() {
        super("The slot overlaps one of your existing slots");
    }
}
