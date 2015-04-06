-- Prevent removing all the roles in a movie
CREATE FUNCTION checkMinRole() RETURNS trigger AS
$$
DECLARE count		INT;
	mov_name	TEXT;
    BEGIN
	-- Select actor count for this movie
	SELECT ActorCount INTO count FROM MovieActorCount
		WHERE ID = OLD.MOVIE_ID;
	-- Check if this actor is last in the movie
        IF count = 1 THEN
		SELECT Name INTO mov_name FROM Movie WHERE ID = OLD.MOVIE_ID;
		RAISE EXCEPTION 'Cannot delete the last role from movie ''%''!', mov_name;
        END IF;
	RETURN OLD;
    END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER MinRole BEFORE DELETE OR UPDATE ON Acting
    FOR EACH ROW EXECUTE PROCEDURE checkMinRole();


-- Prevent removing actor if he's the last one starring in some movie
CREATE FUNCTION checkMinActor() RETURNS trigger AS
$$
DECLARE mov_id		INT;
	count		INT;
	mov_name	TEXT;
    BEGIN
	-- Loop this actors' movies
	FOR mov_id IN SELECT MOVIE_ID FROM Acting WHERE ACTOR_ID = OLD.ID
	LOOP
		-- Select actor count for this movie
		SELECT ActorCount INTO count FROM MovieActorCount
			WHERE ID = mov_id;
		-- Check if movie only has a single actor
		IF count = 1 THEN
			SELECT Name INTO mov_name FROM Movie WHERE ID = mov_id;
			RAISE EXCEPTION 'Cannot delete this actor as he''s the last one starring in ''%''!', mov_name;
		END IF;
	END LOOP;
	RETURN OLD;
    END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER MinActor BEFORE DELETE OR UPDATE ON Actor
    FOR EACH ROW EXECUTE PROCEDURE checkMinActor();

-- Prevent a movie from having more than 5 genres
CREATE FUNCTION checkGenreCount() RETURNS trigger AS
$$
DECLARE count		INT;
    BEGIN
	-- Select actor count for this movie
	SELECT GenreCount INTO count FROM MovieGenreCount
		WHERE ID = NEW.MOVIE_ID;
	-- Check if movie only has a single actor
	IF count = 5 THEN
		RAISE EXCEPTION 'Movie can''t have more than 5 genres';
	END IF;
	RETURN NEW;
    END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER MaxGenre BEFORE INSERT ON Genre
    FOR EACH ROW EXECUTE PROCEDURE checkGenreCount();