package org.example.integrated_architectures.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.integrated_architectures.coach.CoachProfile;

import java.time.LocalDateTime;

/**
 * A time window when a coach is free. Students book it (see Booking).
 * LocalDateTime: no time zone, all users are assumed to be in one zone (simplification).
 */
@Entity
@Table(name = "availability_slots")
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many slots belong to one coach. LAZY: the coach is loaded only when needed.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coach_profile_id", nullable = false)
    private CoachProfile coach;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    protected AvailabilitySlot() {
        // required by JPA
    }

    public AvailabilitySlot(CoachProfile coach, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.coach = coach;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public Long getId() {
        return id;
    }

    public CoachProfile getCoach() {
        return coach;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }
}
