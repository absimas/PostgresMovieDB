CREATE TABLE Movie (
	ID	SERIAL	PRIMARY KEY	CHECK (ID >= 0),
	Name	TEXT	NOT NULL,
	Rating	REAL	DEFAULT 1.0,
	Votes	INTEGER	DEFAULT 1,
	Year	INTEGER CONSTRAINT MovieYearBeforeToday
				CHECK (Year < date_part('year', current_date) + 1)
);

CREATE TABLE Actor (
	ID	SERIAL	PRIMARY KEY,
	Name	TEXT	NOT NULL,
	Surname	TEXT	NOT NULL
);

CREATE TABLE Acting (
	MOVIE_ID	INTEGER	   NOT NULL	CHECK (MOVIE_ID >= 0),
	ACTOR_ID	INTEGER	   NOT NULL	CHECK (ACTOR_ID >= 0),
	PRIMARY KEY	(MOVIE_ID, ACTOR_ID),
	CONSTRAINT fk_movie
		FOREIGN KEY (MOVIE_ID)
		REFERENCES Movie(ID)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
	CONSTRAINT fk_actor
		FOREIGN KEY (ACTOR_ID)
		REFERENCES Actor(ID)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TYPE genre_type AS ENUM ('Action', 'Adventure', 'Adult', 'Animation', 'Biography', 'Comedy', 'Crime', 'Documentary', 'Drama', 'Experimental', 'Fantasy', 'Family', 'Film-Noir', 'Game-Show', 'History', 'Horror', 'Lifestyle', 'Musical', 'Music', 'Mystery', 'News', 'Reality-TV', 'Romance', 'Sci-Fi', 'Short', 'Sport', 'Talk-Show', 'Thriller', 'War', 'Western');

CREATE Table Genre (
	MOVIE_ID	INTEGER	   NOT NULL,
	Genre		genre_type,
	PRIMARY KEY	(MOVIE_ID, Genre),
	CONSTRAINT fk_movie
		FOREIGN KEY (MOVIE_ID)
		REFERENCES Movie(ID)
		On UPDATE CASCADE
		ON DELETE CASCADE
);

CREATE TABLE Quote (
	ID		SERIAL NOT NULL,
	MOVIE_ID	INTEGER	   NOT NULL	CHECK (MOVIE_ID >= 0),
	Quote		TEXT	NOT NULL,
	PRIMARY KEY	(ID, MOVIE_ID),
	CONSTRAINT fk_movie
		FOREIGN KEY (MOVIE_ID)
		REFERENCES Movie(ID)
		ON UPDATE CASCADE
		ON DELETE CASCADE
);