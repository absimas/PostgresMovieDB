-- CONSTRAINTS
-- Movie that'll be released in the future (check constraint violation)
INSERT INTO MOVIE (Name, Rating, Votes, Year) VALUES ('Movie', 1, 1, 2016);

-- Acting with an invalid MOVIE_ID (FK + check constraint violation)
INSERT INTO Actor VALUES (-1, 1);

-- Acting with an invalid ACTOR_ID (FK + check constraint violation)
INSERT INTO Actor VALUES (1, -1);

-- TRIGGERS
-- Prevent removing all the roles in a movie
INSERT INTO Movie VALUES (9999, 'Movie', 1, 1, 1);
INSERT INTO Actor VALUES (99999, 'Name', 'Surname');
INSERT INTO Acting VALUES (9999, 99999);
DELETE FROM Acting WHERE ACTOR_ID=99999;

-- Prevent removing actor if he's the last one starring in some movie
INSERT INTO Movie VALUES (9999, 'Movie', 1, 1, 1);
INSERT INTO Actor VALUES (99999, 'Name', 'Surname');
INSERT INTO Acting VALUES (9999, 99999);
DELETE FROM Actor WHERE ID=99999;

-- Prevent a movie from having more than 5 genres
INSERT INTO Movie VALUES (9999, 'Movie', 1, 1, 1);
INSERT INTO Genre VALUES (9999, 'Biography'), (9999, 'History'), (9999, 'Western'), (9999, 'Documentary'), (9999, 'Adventure');
INSERT INTO Genre VALUES (9999, 'Mystery');

-- DEFAULT VALUES
INSERT INTO Movie (Name, Year) VALUES ('My Movie', 2015);
SELECT * FROM Movie WHERE Name='My Movie' AND Year=2015;