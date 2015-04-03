CREATE TABLE Actor (
	ID	SERIAL	PRIMARY KEY,
	Name	TEXT	NOT NULL,
	Surname	TEXT	NOT NULL
);

CREATE TABLE Movie (
	ID	SERIAL	PRIMARY KEY,
	Name	TEXT	NOT NULL,
	Rating	REAL,
	Votes	INTEGER,
	Year	INTEGER
);

CREATE TABLE Acting (
	MOVIE_ID	INTEGER	   NOT NULL	CHECK(MOVIE_ID >= 0),
	ACTOR_ID	INTEGER	   NOT NULL     CHECK(ACTOR_ID >= 0),
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
	MOVIE_ID	INTEGER	   NOT NULL	CHECK(MOVIE_ID >= 0),
	Genre		genre_type,
	PRIMARY KEY	(MOVIE_ID, Genre),
	CONSTRAINT fk_movie
		FOREIGN KEY (MOVIE_ID)
		REFERENCES Movie(ID)
		On UPDATE CASCADE
		ON DELETE CASCADE
);