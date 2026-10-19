package org.example.integrated_architectures.booking;

public class SlotHasBookingsException extends RuntimeException {

    public SlotHasBookingsException(Long slotId) {
        super("Slot " + slotId + " cannot be deleted: it has bookings");
    }
}
