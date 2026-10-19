-- Time windows when a coach is free for a lesson.
-- TIMESTAMP (without time zone): all users are assumed to be in one time zone (simplification).
CREATE TABLE availability_slots
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    coach_profile_id BIGINT    NOT NULL REFERENCES coach_profiles (id) ON DELETE CASCADE,
    starts_at        TIMESTAMP NOT NULL,
    ends_at          TIMESTAMP NOT NULL,
    CHECK (ends_at > starts_at)
);

-- "Slots of coach X ordered by time" is the most common query.
CREATE INDEX idx_availability_slots_coach_start ON availability_slots (coach_profile_id, starts_at);

-- A student books one slot. The coach is reached through the slot.
CREATE TABLE bookings
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slot_id    BIGINT      NOT NULL REFERENCES availability_slots (id),
    student_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Partial unique index: a slot can have at most ONE active booking (PENDING or CONFIRMED).
-- Cancelled bookings are ignored, so the slot can be booked again after a cancellation.
-- This protects against two students booking the same slot at the same moment.
CREATE UNIQUE INDEX uq_bookings_active_slot ON bookings (slot_id) WHERE status IN ('PENDING', 'CONFIRMED');

CREATE INDEX idx_bookings_student ON bookings (student_id);
