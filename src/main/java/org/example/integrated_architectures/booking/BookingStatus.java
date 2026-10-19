package org.example.integrated_architectures.booking;

/**
 * PENDING   -> CONFIRMED (coach accepts) or CANCELLED (coach declines / student cancels)
 * CONFIRMED -> COMPLETED (lesson took place) or CANCELLED
 * CANCELLED and COMPLETED are final.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}
