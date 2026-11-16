-- This script only seeds demo data the first time it runs (when "movies" is still empty).
-- spring.sql.init.mode=always re-runs this file on every application startup, so without
-- this guard the old unconditional TRUNCATE here would wipe every real order/payment/ticket
-- (via CASCADE) each time the app restarts.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM movies) THEN

        -- Insert Halls
        INSERT INTO halls (name, capacity, description)
        VALUES
        ('Hall A - Premium IMAX', 120, 'State-of-the-art IMAX theater with premium sound system and comfort seating.'),
        ('Hall B - Standard', 100, 'Standard 2D cinema with comfortable seating and good acoustics.'),
        ('Hall C - VIP Lounge', 50, 'VIP cinema with reclining seats and premium amenities.')
        ON CONFLICT (name) DO NOTHING;

        -- Insert Seats for Hall A (12 rows, 10 seats per row = 120 seats)
        -- Regular seats for rows A-J, VIP seats for rows K-L
        INSERT INTO seats (hall_id, row_number, seat_number, type) VALUES
        -- Row A (Regular)
        (1, 'A', 1, 'REGULAR'), (1, 'A', 2, 'REGULAR'), (1, 'A', 3, 'REGULAR'), (1, 'A', 4, 'REGULAR'), (1, 'A', 5, 'REGULAR'),
        (1, 'A', 6, 'REGULAR'), (1, 'A', 7, 'REGULAR'), (1, 'A', 8, 'REGULAR'), (1, 'A', 9, 'REGULAR'), (1, 'A', 10, 'REGULAR'),
        -- Row B (Regular)
        (1, 'B', 1, 'REGULAR'), (1, 'B', 2, 'REGULAR'), (1, 'B', 3, 'REGULAR'), (1, 'B', 4, 'REGULAR'), (1, 'B', 5, 'REGULAR'),
        (1, 'B', 6, 'REGULAR'), (1, 'B', 7, 'REGULAR'), (1, 'B', 8, 'REGULAR'), (1, 'B', 9, 'REGULAR'), (1, 'B', 10, 'REGULAR'),
        -- Row C (Regular)
        (1, 'C', 1, 'REGULAR'), (1, 'C', 2, 'REGULAR'), (1, 'C', 3, 'REGULAR'), (1, 'C', 4, 'REGULAR'), (1, 'C', 5, 'REGULAR'),
        (1, 'C', 6, 'REGULAR'), (1, 'C', 7, 'REGULAR'), (1, 'C', 8, 'REGULAR'), (1, 'C', 9, 'REGULAR'), (1, 'C', 10, 'REGULAR'),
        -- Row D (Regular)
        (1, 'D', 1, 'REGULAR'), (1, 'D', 2, 'REGULAR'), (1, 'D', 3, 'REGULAR'), (1, 'D', 4, 'REGULAR'), (1, 'D', 5, 'REGULAR'),
        (1, 'D', 6, 'REGULAR'), (1, 'D', 7, 'REGULAR'), (1, 'D', 8, 'REGULAR'), (1, 'D', 9, 'REGULAR'), (1, 'D', 10, 'REGULAR'),
        -- Row E (Regular)
        (1, 'E', 1, 'REGULAR'), (1, 'E', 2, 'REGULAR'), (1, 'E', 3, 'REGULAR'), (1, 'E', 4, 'REGULAR'), (1, 'E', 5, 'REGULAR'),
        (1, 'E', 6, 'REGULAR'), (1, 'E', 7, 'REGULAR'), (1, 'E', 8, 'REGULAR'), (1, 'E', 9, 'REGULAR'), (1, 'E', 10, 'REGULAR'),
        -- Row F (Regular)
        (1, 'F', 1, 'REGULAR'), (1, 'F', 2, 'REGULAR'), (1, 'F', 3, 'REGULAR'), (1, 'F', 4, 'REGULAR'), (1, 'F', 5, 'REGULAR'),
        (1, 'F', 6, 'REGULAR'), (1, 'F', 7, 'REGULAR'), (1, 'F', 8, 'REGULAR'), (1, 'F', 9, 'REGULAR'), (1, 'F', 10, 'REGULAR'),
        -- Row G (Regular)
        (1, 'G', 1, 'REGULAR'), (1, 'G', 2, 'REGULAR'), (1, 'G', 3, 'REGULAR'), (1, 'G', 4, 'REGULAR'), (1, 'G', 5, 'REGULAR'),
        (1, 'G', 6, 'REGULAR'), (1, 'G', 7, 'REGULAR'), (1, 'G', 8, 'REGULAR'), (1, 'G', 9, 'REGULAR'), (1, 'G', 10, 'REGULAR'),
        -- Row H (Regular)
        (1, 'H', 1, 'REGULAR'), (1, 'H', 2, 'REGULAR'), (1, 'H', 3, 'REGULAR'), (1, 'H', 4, 'REGULAR'), (1, 'H', 5, 'REGULAR'),
        (1, 'H', 6, 'REGULAR'), (1, 'H', 7, 'REGULAR'), (1, 'H', 8, 'REGULAR'), (1, 'H', 9, 'REGULAR'), (1, 'H', 10, 'REGULAR'),
        -- Row I (Regular)
        (1, 'I', 1, 'REGULAR'), (1, 'I', 2, 'REGULAR'), (1, 'I', 3, 'REGULAR'), (1, 'I', 4, 'REGULAR'), (1, 'I', 5, 'REGULAR'),
        (1, 'I', 6, 'REGULAR'), (1, 'I', 7, 'REGULAR'), (1, 'I', 8, 'REGULAR'), (1, 'I', 9, 'REGULAR'), (1, 'I', 10, 'REGULAR'),
        -- Row J (Regular)
        (1, 'J', 1, 'REGULAR'), (1, 'J', 2, 'REGULAR'), (1, 'J', 3, 'REGULAR'), (1, 'J', 4, 'REGULAR'), (1, 'J', 5, 'REGULAR'),
        (1, 'J', 6, 'REGULAR'), (1, 'J', 7, 'REGULAR'), (1, 'J', 8, 'REGULAR'), (1, 'J', 9, 'REGULAR'), (1, 'J', 10, 'REGULAR'),
        -- Row K (VIP)
        (1, 'K', 1, 'VIP'), (1, 'K', 2, 'VIP'), (1, 'K', 3, 'VIP'), (1, 'K', 4, 'VIP'), (1, 'K', 5, 'VIP'),
        (1, 'K', 6, 'VIP'), (1, 'K', 7, 'VIP'), (1, 'K', 8, 'VIP'), (1, 'K', 9, 'VIP'), (1, 'K', 10, 'VIP'),
        -- Row L (VIP)
        (1, 'L', 1, 'VIP'), (1, 'L', 2, 'VIP'), (1, 'L', 3, 'VIP'), (1, 'L', 4, 'VIP'), (1, 'L', 5, 'VIP'),
        (1, 'L', 6, 'VIP'), (1, 'L', 7, 'VIP'), (1, 'L', 8, 'VIP'), (1, 'L', 9, 'VIP'), (1, 'L', 10, 'VIP');

        -- Insert Seats for Hall B (10 rows, 10 seats per row = 100 seats)
        -- All regular seats
        INSERT INTO seats (hall_id, row_number, seat_number, type) VALUES
        -- Row A
        (2, 'A', 1, 'REGULAR'), (2, 'A', 2, 'REGULAR'), (2, 'A', 3, 'REGULAR'), (2, 'A', 4, 'REGULAR'), (2, 'A', 5, 'REGULAR'),
        (2, 'A', 6, 'REGULAR'), (2, 'A', 7, 'REGULAR'), (2, 'A', 8, 'REGULAR'), (2, 'A', 9, 'REGULAR'), (2, 'A', 10, 'REGULAR'),
        -- Row B
        (2, 'B', 1, 'REGULAR'), (2, 'B', 2, 'REGULAR'), (2, 'B', 3, 'REGULAR'), (2, 'B', 4, 'REGULAR'), (2, 'B', 5, 'REGULAR'),
        (2, 'B', 6, 'REGULAR'), (2, 'B', 7, 'REGULAR'), (2, 'B', 8, 'REGULAR'), (2, 'B', 9, 'REGULAR'), (2, 'B', 10, 'REGULAR'),
        -- Row C
        (2, 'C', 1, 'REGULAR'), (2, 'C', 2, 'REGULAR'), (2, 'C', 3, 'REGULAR'), (2, 'C', 4, 'REGULAR'), (2, 'C', 5, 'REGULAR'),
        (2, 'C', 6, 'REGULAR'), (2, 'C', 7, 'REGULAR'), (2, 'C', 8, 'REGULAR'), (2, 'C', 9, 'REGULAR'), (2, 'C', 10, 'REGULAR'),
        -- Row D
        (2, 'D', 1, 'REGULAR'), (2, 'D', 2, 'REGULAR'), (2, 'D', 3, 'REGULAR'), (2, 'D', 4, 'REGULAR'), (2, 'D', 5, 'REGULAR'),
        (2, 'D', 6, 'REGULAR'), (2, 'D', 7, 'REGULAR'), (2, 'D', 8, 'REGULAR'), (2, 'D', 9, 'REGULAR'), (2, 'D', 10, 'REGULAR'),
        -- Row E
        (2, 'E', 1, 'REGULAR'), (2, 'E', 2, 'REGULAR'), (2, 'E', 3, 'REGULAR'), (2, 'E', 4, 'REGULAR'), (2, 'E', 5, 'REGULAR'),
        (2, 'E', 6, 'REGULAR'), (2, 'E', 7, 'REGULAR'), (2, 'E', 8, 'REGULAR'), (2, 'E', 9, 'REGULAR'), (2, 'E', 10, 'REGULAR'),
        -- Row F
        (2, 'F', 1, 'REGULAR'), (2, 'F', 2, 'REGULAR'), (2, 'F', 3, 'REGULAR'), (2, 'F', 4, 'REGULAR'), (2, 'F', 5, 'REGULAR'),
        (2, 'F', 6, 'REGULAR'), (2, 'F', 7, 'REGULAR'), (2, 'F', 8, 'REGULAR'), (2, 'F', 9, 'REGULAR'), (2, 'F', 10, 'REGULAR'),
        -- Row G
        (2, 'G', 1, 'REGULAR'), (2, 'G', 2, 'REGULAR'), (2, 'G', 3, 'REGULAR'), (2, 'G', 4, 'REGULAR'), (2, 'G', 5, 'REGULAR'),
        (2, 'G', 6, 'REGULAR'), (2, 'G', 7, 'REGULAR'), (2, 'G', 8, 'REGULAR'), (2, 'G', 9, 'REGULAR'), (2, 'G', 10, 'REGULAR'),
        -- Row H
        (2, 'H', 1, 'REGULAR'), (2, 'H', 2, 'REGULAR'), (2, 'H', 3, 'REGULAR'), (2, 'H', 4, 'REGULAR'), (2, 'H', 5, 'REGULAR'),
        (2, 'H', 6, 'REGULAR'), (2, 'H', 7, 'REGULAR'), (2, 'H', 8, 'REGULAR'), (2, 'H', 9, 'REGULAR'), (2, 'H', 10, 'REGULAR'),
        -- Row I
        (2, 'I', 1, 'REGULAR'), (2, 'I', 2, 'REGULAR'), (2, 'I', 3, 'REGULAR'), (2, 'I', 4, 'REGULAR'), (2, 'I', 5, 'REGULAR'),
        (2, 'I', 6, 'REGULAR'), (2, 'I', 7, 'REGULAR'), (2, 'I', 8, 'REGULAR'), (2, 'I', 9, 'REGULAR'), (2, 'I', 10, 'REGULAR'),
        -- Row J
        (2, 'J', 1, 'REGULAR'), (2, 'J', 2, 'REGULAR'), (2, 'J', 3, 'REGULAR'), (2, 'J', 4, 'REGULAR'), (2, 'J', 5, 'REGULAR'),
        (2, 'J', 6, 'REGULAR'), (2, 'J', 7, 'REGULAR'), (2, 'J', 8, 'REGULAR'), (2, 'J', 9, 'REGULAR'), (2, 'J', 10, 'REGULAR');

        -- Insert Seats for Hall C - VIP Lounge (5 rows, 10 seats per row = 50 seats)
        -- All VIP seats
        INSERT INTO seats (hall_id, row_number, seat_number, type) VALUES
        -- Row A
        (3, 'A', 1, 'VIP'), (3, 'A', 2, 'VIP'), (3, 'A', 3, 'VIP'), (3, 'A', 4, 'VIP'), (3, 'A', 5, 'VIP'),
        (3, 'A', 6, 'VIP'), (3, 'A', 7, 'VIP'), (3, 'A', 8, 'VIP'), (3, 'A', 9, 'VIP'), (3, 'A', 10, 'VIP'),
        -- Row B
        (3, 'B', 1, 'VIP'), (3, 'B', 2, 'VIP'), (3, 'B', 3, 'VIP'), (3, 'B', 4, 'VIP'), (3, 'B', 5, 'VIP'),
        (3, 'B', 6, 'VIP'), (3, 'B', 7, 'VIP'), (3, 'B', 8, 'VIP'), (3, 'B', 9, 'VIP'), (3, 'B', 10, 'VIP'),
        -- Row C
        (3, 'C', 1, 'VIP'), (3, 'C', 2, 'VIP'), (3, 'C', 3, 'VIP'), (3, 'C', 4, 'VIP'), (3, 'C', 5, 'VIP'),
        (3, 'C', 6, 'VIP'), (3, 'C', 7, 'VIP'), (3, 'C', 8, 'VIP'), (3, 'C', 9, 'VIP'), (3, 'C', 10, 'VIP'),
        -- Row D
        (3, 'D', 1, 'VIP'), (3, 'D', 2, 'VIP'), (3, 'D', 3, 'VIP'), (3, 'D', 4, 'VIP'), (3, 'D', 5, 'VIP'),
        (3, 'D', 6, 'VIP'), (3, 'D', 7, 'VIP'), (3, 'D', 8, 'VIP'), (3, 'D', 9, 'VIP'), (3, 'D', 10, 'VIP'),
        -- Row E
        (3, 'E', 1, 'VIP'), (3, 'E', 2, 'VIP'), (3, 'E', 3, 'VIP'), (3, 'E', 4, 'VIP'), (3, 'E', 5, 'VIP'),
        (3, 'E', 6, 'VIP'), (3, 'E', 7, 'VIP'), (3, 'E', 8, 'VIP'), (3, 'E', 9, 'VIP'), (3, 'E', 10, 'VIP');

        INSERT INTO movies (title, description, genre, duration, poster_url, trailer_url, release_date, director, movie_cast, language, active, created_at, updated_at) VALUES
        ('Oppenheimer', 'A biopic about J. Robert Oppenheimer and the creation of the atomic bomb.', 'Drama', 180, 'https://upload.wikimedia.org/wikipedia/sr/8/8c/Openhajmer_film.jpg', 'https://www.youtube.com/embed/pp8q4Z9T3RM', '2023-07-21', 'Christopher Nolan', 'Cillian Murphy, Emily Blunt', 'English', true, NOW(), NOW()),
        ('Barbie', 'A whimsical story set in the world of Barbie.', 'Comedy', 114, 'https://upload.wikimedia.org/wikipedia/en/0/0b/Barbie_2023_poster.jpg', 'https://www.youtube.com/embed/9BkuatuuTwk', '2023-07-21', 'Greta Gerwig', 'Margot Robbie, Ryan Gosling', 'English', true, NOW(), NOW()),
        ('The Dark Knight Rises', 'When a new enemy called Bane emerges, Batman must confront his greatest fear to save Gotham City from destruction.', 'Action', 165, 'https://upload.wikimedia.org/wikipedia/en/8/83/Dark_knight_rises_poster.jpg', 'https://www.youtube.com/embed/B7XoW6n3Y5E', '2012-07-20', 'Christopher Nolan', 'Christian Bale, Michael Caine, Joseph Gordon-Levitt', 'English', true, NOW(), NOW()),
        ('Inception', 'A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.', 'Sci-Fi', 148, 'https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg', 'https://www.youtube.com/embed/YoHD9XEInc0', '2010-07-16', 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt', 'English', true, NOW(), NOW()),
        ('Interstellar', 'Explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.', 'Sci-Fi', 169, 'https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg', 'https://www.youtube.com/embed/zSWdZVtXT7E', '2014-11-07', 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway', 'English', true, NOW(), NOW()),
        ('Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster''s wife and a pair of diner bandits intertwine.', 'Crime', 154, 'https://upload.wikimedia.org/wikipedia/en/3/3b/Pulp_Fiction_%281994%29_poster.jpg', 'https://www.youtube.com/embed/s7EdQ4FqbhY', '1994-10-14', 'Quentin Tarantino', 'John Travolta, Samuel L. Jackson', 'English', true, NOW(), NOW()),
        ('The Shawshank Redemption', 'Two imprisoned men bond over years, finding solace and eventual redemption.', 'Drama', 142, 'https://upload.wikimedia.org/wikipedia/en/8/81/ShawshankRedemptionMoviePoster.jpg', 'https://www.youtube.com/embed/6hB3S9bIaco', '1994-09-23', 'Frank Darabont', 'Tim Robbins, Morgan Freeman', 'English', true, NOW(), NOW()),
        ('The Matrix', 'A hacker discovers the nature of reality and his role in the war against its controllers.', 'Sci-Fi', 136, 'https://upload.wikimedia.org/wikipedia/en/d/db/The_Matrix.png', 'https://www.youtube.com/embed/vKQi3bBA1y8', '1999-03-31', 'The Wachowskis', 'Keanu Reeves, Laurence Fishburne', 'English', true, NOW(), NOW()),
        ('Forrest Gump', 'The presidencies, Vietnam, Watergate and other history unfold through the perspective of an Alabama man.', 'Drama', 142, 'https://upload.wikimedia.org/wikipedia/en/6/67/Forrest_Gump_poster.jpg', 'https://www.youtube.com/embed/bLvqoHBptjg', '1994-07-06', 'Robert Zemeckis', 'Tom Hanks, Robin Wright', 'English', true, NOW(), NOW()),
        ('Gladiator', 'A former Roman General sets out to exact vengeance against the corrupt emperor.', 'Action', 155, 'https://upload.wikimedia.org/wikipedia/sr/thumb/f/fe/Gladiatorteaser.jpg/500px-Gladiatorteaser.jpg', 'https://www.youtube.com/embed/owK1qxDselE', '2000-05-05', 'Ridley Scott', 'Russell Crowe, Joaquin Phoenix', 'English', true, NOW(), NOW()),
        ('Avatar', 'A paraplegic Marine is dispatched to the moon Pandora on a unique mission.', 'Adventure', 162, 'https://upload.wikimedia.org/wikipedia/sr/0/01/AvatarPoster.jpg', 'https://www.youtube.com/embed/5PSNL1qE6VY', '2009-12-18', 'James Cameron', 'Sam Worthington, Zoe Saldana', 'English', true, NOW(), NOW()),
        ('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control to his reluctant son.', 'Crime', 175, 'https://upload.wikimedia.org/wikipedia/en/1/1c/Godfather_ver1.jpg', 'https://www.youtube.com/embed/sY1S34973zA', '1972-03-24', 'Francis Ford Coppola', 'Marlon Brando, Al Pacino', 'English', true, NOW(), NOW()),
        ('Top Gun: Maverick', 'After more than thirty years, Pete Mitchell returns to train a new generation of pilots.', 'Action', 130, 'https://upload.wikimedia.org/wikipedia/en/1/13/Top_Gun_Maverick_Poster.jpg', 'https://www.youtube.com/embed/giXco2jaZ_4', '2022-05-27', 'Joseph Kosinski', 'Tom Cruise, Miles Teller', 'English', true, NOW(), NOW()),
        ('Dune', 'Feature adaptation of Frank Herbert''s science fiction novel.', 'Sci-Fi', 155, 'https://upload.wikimedia.org/wikipedia/en/8/8e/Dune_%282021_film%29.jpg', 'https://www.youtube.com/embed/8g18jFHCLXk', '2021-10-22', 'Denis Villeneuve', 'Timothée Chalamet, Zendaya', 'English', true, NOW(), NOW()),
        ('Spider-Man: No Way Home', 'Spider-Man seeks help from Doctor Strange to fix his identity reveal.', 'Action', 148, 'https://upload.wikimedia.org/wikipedia/en/0/00/Spider-Man_No_Way_Home_poster.jpg', 'https://www.youtube.com/embed/JfVOs4VSpmA', '2021-12-17', 'Jon Watts', 'Tom Holland, Zendaya', 'English', true, NOW(), NOW());

        INSERT INTO screenings (movie_id, hall_id, screening_date, screening_time, price, available_seats, active, created_at, updated_at) VALUES
        (1, 1, CURRENT_DATE + INTERVAL '1 day', '10:00:00', 12.50, 95, true, NOW(), NOW()),
        (2, 1, CURRENT_DATE + INTERVAL '1 day', '11:00:00', 12.50, 110, true, NOW(), NOW()),
        (3, 2, CURRENT_DATE + INTERVAL '1 day', '20:00:00', 12.50, 88, true, NOW(), NOW()),
        (4, 1, CURRENT_DATE + INTERVAL '1 day', '15:00:00', 12.50, 100, true, NOW(), NOW()),
        (5, 1, CURRENT_DATE + INTERVAL '2 day', '12:00:00', 12.50, 108, true, NOW(), NOW()),
        (6, 1, CURRENT_DATE + INTERVAL '3 day', '16:00:00', 12.50, 112, true, NOW(), NOW()),
        (7, 3, CURRENT_DATE + INTERVAL '1 day', '14:30:00', 18.00, 49, true, NOW(), NOW()),
        (8, 2, CURRENT_DATE + INTERVAL '1 day', '21:00:00', 12.50, 94, true, NOW(), NOW()),
        (9, 1, CURRENT_DATE + INTERVAL '2 day', '16:00:00', 12.50, 106, true, NOW(), NOW()),
        (10, 2, CURRENT_DATE + INTERVAL '1 day', '16:00:00', 12.50, 91, true, NOW(), NOW()),
        (11, 1, CURRENT_DATE + INTERVAL '1 day', '12:30:00', 14.00, 99, true, NOW(), NOW()),
        (12, 1, CURRENT_DATE + INTERVAL '1 day', '19:00:00', 12.50, 103, true, NOW(), NOW()),
        (13, 2, CURRENT_DATE + INTERVAL '1 day', '13:00:00', 12.50, 86, true, NOW(), NOW()),
        (14, 1, CURRENT_DATE + INTERVAL '1 day', '14:00:00', 14.00, 101, true, NOW(), NOW()),
        (15, 2, CURRENT_DATE + INTERVAL '1 day', '11:30:00', 12.50, 84, true, NOW(), NOW());

        INSERT INTO orders (user_id, status, total_price, order_date, created_at, updated_at) VALUES
        (1, 'PENDING', 25.00, NOW(), NOW(), NOW()),
        (1, 'PENDING', 50.00, NOW(), NOW(), NOW()),
        (2, 'PENDING', 37.50, NOW(), NOW(), NOW());

        INSERT INTO order_items (order_id, screening_id, seat_id, quantity, price) VALUES
        (1, 1, 1, 2, 12.50),
        (1, 1, 2, 1, 12.50),
        (2, 2, 101, 4, 12.50),
        (3, 3, 201, 3, 12.50);

    END IF;
END $$;;
