-- Users of all roles. A coach starts as PENDING and can log in only after an admin sets ACTIVE.
CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('STUDENT', 'COACH', 'ADMIN')),
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE', 'PENDING', 'REJECTED')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE sports
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Extra data for users with role COACH (1:1 with users).
CREATE TABLE coach_profiles
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT        NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    bio              VARCHAR(2000),
    experience_years INT           NOT NULL CHECK (experience_years >= 0),
    hourly_price     NUMERIC(8, 2) NOT NULL CHECK (hourly_price > 0),
    city             VARCHAR(100),
    online           BOOLEAN       NOT NULL DEFAULT TRUE,
    rejection_reason VARCHAR(500)
);

-- Which sports a coach teaches (M:N).
CREATE TABLE coach_profile_sports
(
    coach_profile_id BIGINT NOT NULL REFERENCES coach_profiles (id) ON DELETE CASCADE,
    sport_id         BIGINT NOT NULL REFERENCES sports (id),
    PRIMARY KEY (coach_profile_id, sport_id)
);
