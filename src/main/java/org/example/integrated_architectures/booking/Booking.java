package org.example.integrated_architectures.booking;

import jakarta.persistence.*;
import org.example.integrated_architectures.user.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A student's booking of one availability slot. The coach is reached via slot.getCoach().
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many bookings (over time, e.g. after cancellations) can point to one slot.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private AvailabilitySlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {
        // required by JPA
    }

    // A new booking always waits for the coach's confirmation.
    public Booking(AvailabilitySlot slot, User student) {
        this.slot = slot;
        this.student = student;
        this.status = BookingStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public AvailabilitySlot getSlot() {
        return slot;
    }

    public User getStudent() {
        return student;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

}
