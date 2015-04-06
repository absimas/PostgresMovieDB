-- Movies including their age (in years) instead of the release year
CREATE VIEW MovieAge AS
	SELECT ID, Name, Rating, Votes, 
		EXTRACT(YEAR FROM 
			AGE(
				TO_TIMESTAMP(Year || '-01-01', 'YYYY-MM-DD')
			)
		) AS Age
	FROM Movie;

-- Movies including their actor count
CREATE VIEW MovieActorCount AS
	SELECT ID, Name, Rating, Votes, Year, 
		(SELECT COUNT(*) FROM Acting WHERE MOVIE_ID=ID) AS ActorCount
	FROM Movie;

-- Movies including their genre count
CREATE VIEW MovieGenreCount AS
	SELECT ID, Name, Rating, Votes, Year, 
		(SELECT COUNT(*) FROM Genre WHERE MOVIE_ID=ID) AS GenreCount
	FROM Movie;