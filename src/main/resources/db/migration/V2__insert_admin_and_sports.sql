-- Initial admin account. Password: admin123 (development only!)
INSERT INTO users (email, password_hash, first_name, last_name, role, status)
VALUES ('admin@sportcoach.local', '$2y$10$yN8dNbKzDSRmn7bHNffdk.zctSreXD/PFdc5lYXn3h0/Kms39iCBS',
        'Admin', 'SportCoach', 'ADMIN', 'ACTIVE');

INSERT INTO sports (name)
VALUES ('Tennis'),
       ('Boxing'),
       ('Yoga'),
       ('Swimming'),
       ('Running'),
       ('Fitness'),
       ('Football'),
       ('Chess');
