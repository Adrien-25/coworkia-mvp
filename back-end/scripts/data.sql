-- Test Data for Coworkia MVP (Paris République Pilot)

-- 1. Insert Site
INSERT INTO sites (name, address, city) VALUES ('Paris République', '12 rue de la Liberté', 'Paris');

-- 2. Insert Zones for Paris République
INSERT INTO zones (site_id, name, code, type, capacity) VALUES 
(1, 'Atlas', 'AT', 'OPEN_SPACE', 20),
(1, 'Boréal', 'BO', 'OPEN_SPACE', 20),
(1, 'Calypso', 'CA', 'OPEN_SPACE', 15),
(1, 'Delta', 'DE', 'OPEN_SPACE', 15),
(1, 'Echo', 'EC', 'PRIVATE_OFFICE', 10),
(1, 'Fjord', 'FJ', 'PRIVATE_OFFICE', 10),
(1, 'Gaia', 'GA', 'MEETING_ROOM', 5),
(1, 'Hélios', 'HE', 'MEETING_ROOM', 5);

-- 3. Insert Sample Desks (A few for each zone to keep the script concise)
-- Atlas [AT]
INSERT INTO desks (zone_id, code) VALUES (1, 'AT-A'), (1, 'AT-B'), (1, 'AT-C');
-- Boréal [BO]
INSERT INTO desks (zone_id, code) VALUES (2, 'BO-A'), (2, 'BO-B');
-- Delta [DE]
INSERT INTO desks (zone_id, code) VALUES (4, 'DE-A'), (4, 'DE-B');
-- Gaia [GA] (Meeting rooms usually booked as a whole or by seat, here we treat them as individual bookable resources if needed, or the zone itself is the resource)
INSERT INTO desks (zone_id, code) VALUES (7, 'GA-A');

-- 4. Insert Sample Users (Password is 'password' encoded with BCrypt $2a$10$...)
INSERT INTO users (email, password, first_name, last_name, role, fidelity_points) VALUES 
('admin@coworkia.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uKCv1.', 'Marc', 'Dupont', 'ROLE_ADMIN', 0),
('manager@coworkia.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uKCv1.', 'Julie', 'Martin', 'ROLE_MANAGER', 0),
('user@coworkia.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.7uKCv1.', 'Sarah', 'Dubois', 'ROLE_USER', 450);

-- 5. Insert Sample Bookings
INSERT INTO bookings (user_id, desk_id, start_time, end_time, price) VALUES 
(3, 1, '2026-03-10 09:00:00', '2026-03-10 18:00:00', 25.00),
(3, 8, '2026-03-12 14:00:00', '2026-03-12 16:00:00', 70.00);

-- 6. Insert Sample Invoices
INSERT INTO invoices (booking_id, amount, is_paid) VALUES 
(1, 25.00, true),
(2, 70.00, false);
