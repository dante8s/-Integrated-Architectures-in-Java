-- Optional short text shown in the sport list and catalog filters.
ALTER TABLE sports
    ADD COLUMN description VARCHAR(500);

UPDATE sports SET description = 'Singles and doubles, technique and match tactics' WHERE name = 'Tennis';
UPDATE sports SET description = 'Footwork, punches, defence and conditioning' WHERE name = 'Boxing';
UPDATE sports SET description = 'Flexibility, balance and breathing' WHERE name = 'Yoga';
UPDATE sports SET description = 'From learning to swim to competition strokes' WHERE name = 'Swimming';
UPDATE sports SET description = 'Training plans from 5K to marathon' WHERE name = 'Running';
UPDATE sports SET description = 'Strength and general fitness, gym or home' WHERE name = 'Fitness';
UPDATE sports SET description = 'Ball control, passing and individual skills' WHERE name = 'Football';
UPDATE sports SET description = 'Openings, tactics and endgames' WHERE name = 'Chess';
