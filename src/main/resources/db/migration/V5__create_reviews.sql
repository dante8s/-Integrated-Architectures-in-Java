-- A student's review of one COMPLETED lesson (booking). The coach is reached via booking -> slot -> coach.
-- UNIQUE (booking_id): one review per lesson, guaranteed by the database (e.g. against a double click).
CREATE TABLE reviews
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id BIGINT      NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
    rating     INTEGER     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
