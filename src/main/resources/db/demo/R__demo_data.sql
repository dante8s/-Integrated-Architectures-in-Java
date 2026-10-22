-- Demo data for showing the app (coaches, slots, bookings, reviews).
-- Loaded ONLY with the "demo" profile (application-demo.properties adds classpath:db/demo to Flyway).
-- "R__" = repeatable migration: Flyway runs it after all V__ migrations, so future V6, V7... are not affected.
-- All demo accounts have the password: demo1234
-- Slot times are relative to the day of the first start, so "future" slots are really in the future.
DO
$demo$
    DECLARE
        pwd    CONSTANT VARCHAR   := '$2a$10$s2ISS.wcfCxzhfzbAziXw.33BdGet5Cu.AIqdZ8o1OeUJUGMlomJa';
        today  CONSTANT TIMESTAMP := date_trunc('day', LOCALTIMESTAMP);
        uid    BIGINT;
        andrii BIGINT;
        iryna  BIGINT;
        oleh   BIGINT;
        maria  BIGINT;
        taras  BIGINT;
        dmytro BIGINT;
        olena  BIGINT;
        maksym BIGINT;
        slot   BIGINT;
        bkg    BIGINT;
    BEGIN
        -- Already loaded (e.g. the file was re-run after a change) -> do nothing.
        IF EXISTS (SELECT 1 FROM users WHERE email = 'student@demo.local') THEN
            RETURN;
        END IF;

        -- ---------- students ----------
        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('student@demo.local', pwd, 'Olena', 'Petrenko', 'STUDENT', 'ACTIVE')
        RETURNING id INTO olena;
        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('student2@demo.local', pwd, 'Maksym', 'Tkachenko', 'STUDENT', 'ACTIVE')
        RETURNING id INTO maksym;

        -- ---------- active coaches ----------
        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('coach@demo.local', pwd, 'Andrii', 'Kovalenko', 'COACH', 'ACTIVE')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Former professional tennis player. Technique, serve and match tactics for beginners and club players.',
                8, 25.00, 'Kyiv')
        RETURNING id INTO andrii;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT andrii, id FROM sports WHERE name IN ('Tennis', 'Fitness');

        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('coach2@demo.local', pwd, 'Iryna', 'Melnyk', 'COACH', 'ACTIVE')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Hatha and vinyasa yoga. Flexibility, breathing and posture.', 5, 18.00, 'Lviv')
        RETURNING id INTO iryna;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT iryna, id FROM sports WHERE name IN ('Yoga', 'Fitness');

        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('coach3@demo.local', pwd, 'Oleh', 'Bondarenko', 'COACH', 'ACTIVE')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Boxing coach, master of sports. Footwork, combinations, conditioning.', 12, 30.00, 'Kharkiv')
        RETURNING id INTO oleh;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT oleh, id FROM sports WHERE name IN ('Boxing', 'Running');

        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('coach4@demo.local', pwd, 'Mariia', 'Shevchenko', 'COACH', 'ACTIVE')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Swimming for kids and adults: from the first strokes to competitions.', 6, 22.00, 'Odesa')
        RETURNING id INTO maria;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT maria, id FROM sports WHERE name = 'Swimming';

        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('coach5@demo.local', pwd, 'Taras', 'Hnatiuk', 'COACH', 'ACTIVE')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Chess openings and endgames, online lessons.', 15, 15.00, NULL)
        RETURNING id INTO taras;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT taras, id FROM sports WHERE name = 'Chess';

        -- ---------- a coach waiting for the admin's approval (Admin -> Coach approval) ----------
        INSERT INTO users (email, password_hash, first_name, last_name, role, status)
        VALUES ('pending@demo.local', pwd, 'Dmytro', 'Lysenko', 'COACH', 'PENDING')
        RETURNING id INTO uid;
        INSERT INTO coach_profiles (user_id, bio, experience_years, hourly_price, city)
        VALUES (uid, 'Marathon runner, running technique and training plans.', 4, 20.00, 'Dnipro')
        RETURNING id INTO dmytro;
        INSERT INTO coach_profile_sports (coach_profile_id, sport_id)
        SELECT dmytro, id FROM sports WHERE name = 'Running';

        -- ---------- free future slots ----------
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        SELECT c.id, today + d * INTERVAL '1 day' + h * INTERVAL '1 hour',
               today + d * INTERVAL '1 day' + (h + 1) * INTERVAL '1 hour'
        FROM (VALUES (andrii), (iryna), (oleh), (maria), (taras)) AS c(id),
             generate_series(3, 7) AS d,          -- in 3..7 days
             (VALUES (10), (16)) AS t(h);         -- at 10:00 and 16:00

        -- ---------- bookings of coach Andrii (log in as coach@demo.local) ----------
        -- PENDING: a new request the coach can confirm or decline
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today + INTERVAL '1 day 12 hours', today + INTERVAL '1 day 13 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, olena, 'PENDING');

        -- CONFIRMED, in the future: the student can still cancel it
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today + INTERVAL '2 days 12 hours', today + INTERVAL '2 days 13 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, maksym, 'CONFIRMED');

        -- CONFIRMED, already ended: the coach can mark it as completed
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today - INTERVAL '2 days' + INTERVAL '12 hours', today - INTERVAL '2 days' + INTERVAL '13 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, maksym, 'CONFIRMED');

        -- COMPLETED without a review: Olena can leave a review
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today - INTERVAL '3 days' + INTERVAL '10 hours', today - INTERVAL '3 days' + INTERVAL '11 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, olena, 'COMPLETED');

        -- COMPLETED with reviews
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today - INTERVAL '10 days' + INTERVAL '10 hours', today - INTERVAL '10 days' + INTERVAL '11 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, olena, 'COMPLETED') RETURNING id INTO bkg;
        INSERT INTO reviews (booking_id, rating, comment)
        VALUES (bkg, 5, 'Great first lesson! Clear explanations, my serve is much better now.');

        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today - INTERVAL '8 days' + INTERVAL '16 hours', today - INTERVAL '8 days' + INTERVAL '17 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, maksym, 'COMPLETED') RETURNING id INTO bkg;
        INSERT INTO reviews (booking_id, rating, comment) VALUES (bkg, 4, 'Good coach, a bit too intense for me.');

        -- CANCELLED: shown in the history
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (andrii, today - INTERVAL '5 days' + INTERVAL '10 hours', today - INTERVAL '5 days' + INTERVAL '11 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, olena, 'CANCELLED');

        -- ---------- a review for coach Iryna ----------
        INSERT INTO availability_slots (coach_profile_id, starts_at, ends_at)
        VALUES (iryna, today - INTERVAL '6 days' + INTERVAL '9 hours', today - INTERVAL '6 days' + INTERVAL '10 hours')
        RETURNING id INTO slot;
        INSERT INTO bookings (slot_id, student_id, status) VALUES (slot, olena, 'COMPLETED') RETURNING id INTO bkg;
        INSERT INTO reviews (booking_id, rating, comment) VALUES (bkg, 5, 'Very calm and attentive teacher.');
    END
$demo$;
