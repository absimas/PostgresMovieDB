package com.simas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Simas Abramovas on 2015 Apr 02.
 */

public class DB {

	// Database TEXT INDEXES are LOWERCASE so convert any string to lower case before searching!

	// Auth
//	private static final String DB_HOST = "192.168.42.51";
//	private static final String DB_NAME = "studentu";
//	private static final String DB_LOGIN = "siab1555";
//	private static final String DB_PASS = "siab1555";
	private static final String DB_HOST = "localhost";
	private static final String DB_NAME = "pmdb";
	private static final String DB_LOGIN = "simas";
	private static final String DB_PASS = "123";
	// Tables
	private static final String TABLE_MOVIE = "Movie";
	private static final String TABLE_GENRE = "Genre";
	private static final String TABLE_ACTOR = "Actor";
	private static final String TABLE_ACTING = "Acting";
	// Columns
	private static final String COL_ID = "ID";
	private static final String COL_MOVIE_ID = "MOVIE_ID";
	private static final String COL_ACTOR_ID = "ACTOR_ID";
	private static final String COL_GENRE = "Genre";
	private static final String COL_ACTOR_NAME = "Name";
	private static final String COL_ACTOR_SURNAME = "Surname";
	private static final String COL_NAME = "Name";
	private static final String COL_RATING = "Rating";
	private static final String COL_VOTES = "Votes";
	private static final String COL_YEAR = "Year";

	private Connection mConnection;
	// Select queries
	private PreparedStatement mSelectActorInterval;
	private PreparedStatement mSelectActors;
	private PreparedStatement mSelectMovieInterval;
	private PreparedStatement mSelectGenresByMovieId;
	private PreparedStatement mSelectActorIntervalByMovieId;
	private PreparedStatement mSelectMovieIntervalByActorId;
	private PreparedStatement mSelectMovieById;
	private PreparedStatement mSelectActorById;
	// Update queries
	private PreparedStatement mUpdateMovieById;
	private PreparedStatement mUpdateActorById;
	// Delete queries
	private PreparedStatement mDeleteMovieById;
	private PreparedStatement mDeleteActorById;
	private PreparedStatement mDeleteActingByMovieId;
	private PreparedStatement mDeleteActingByActorId;
	// Insert queries
	private PreparedStatement mInsertMovie;
	private PreparedStatement mInsertActor;
	private PreparedStatement mInsertActing;
	private PreparedStatement mInsertGenreByMovieAndGenre;
	private PreparedStatement mInsertGenre;
	// Search queries
	private PreparedStatement mSelectMovieByName;
	private PreparedStatement mSelectActorByNameOrSurname;
	private PreparedStatement mSelectActorByNameOrSurnameAndMovieId;

	public DB() {
		try {
			// Connect to DB
			String url = String.format("jdbc:postgresql://%s/%s?user=%s&password=%s",
					DB_HOST, DB_NAME, DB_LOGIN, DB_PASS);
			mConnection = DriverManager.getConnection(url);
			initStatements();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void initStatements() {
		// Do somth
		try {
			// Interval selections
			mSelectActorInterval = mConnection.prepareStatement(String
					.format("SELECT * FROM %s LIMIT ? OFFSET ?", TABLE_ACTOR));
			mSelectMovieInterval = mConnection.prepareStatement(String
					.format("SELECT * FROM %s LIMIT ? OFFSET ?", TABLE_MOVIE));
			mSelectActors = mConnection.prepareStatement(String
					.format("SELECT * FROM %s", TABLE_ACTOR));
			// SELECT * FROM Actor WHERE ID = IN (
			//     SELECT ACTOR_ID FROM Acting WHERE MOVIE_ID=?
			// ) LIMIT ? OFFSET ?
			mSelectActorIntervalByMovieId = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE %s IN (" +
									"SELECT %s FROM %s WHERE %s=?" +
									") LIMIT ? OFFSET ?",
							TABLE_ACTOR, COL_ID, COL_ACTOR_ID, TABLE_ACTING, COL_MOVIE_ID));
			// SELECT * FROM Movie WHERE ID = IN (
			//     SELECT MOVIE_ID FROM Acting WHERE ACTOR_ID=?
			// ) LIMIT ? OFFSET ?
			mSelectMovieIntervalByActorId = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE %s=IN(" +
									"SELECT %s FROM %s WHERE %s=?" +
									") LIMIT ? OFFSET ?",
							TABLE_MOVIE, COL_ID, COL_MOVIE_ID, TABLE_ACTING, COL_ACTOR_ID));
			mSelectGenresByMovieId = mConnection.prepareStatement(String
					.format("SELECT %s FROM %s WHERE %s=?", COL_GENRE, TABLE_GENRE, COL_MOVIE_ID));
			mSelectMovieById = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE %s=?", TABLE_MOVIE, COL_ID));
			mSelectActorById = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE %s=?", TABLE_ACTOR, COL_ID));
			/**************************************************************************************/
			mUpdateMovieById = mConnection.prepareStatement(String
					.format("UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ?",
							TABLE_MOVIE, COL_NAME, COL_YEAR, COL_RATING, COL_VOTES, COL_ID));
			mUpdateActorById = mConnection.prepareStatement(String
					.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?",
							TABLE_ACTOR, COL_ACTOR_NAME, COL_ACTOR_SURNAME, COL_ACTOR_ID));
			/**************************************************************************************/
			mDeleteMovieById = mConnection.prepareStatement(String
					.format("DELETE FROM %s WHERE %s = ?", TABLE_MOVIE, COL_ID));
			mDeleteActorById = mConnection.prepareStatement(String
					.format("DELETE FROM %s WHERE %s = ?", TABLE_ACTOR, COL_ID));
			mDeleteActingByMovieId = mConnection.prepareStatement(String
					.format("DELETE FROM %s WHERE %s = ?", TABLE_ACTING, COL_MOVIE_ID));
			mDeleteActingByActorId = mConnection.prepareStatement(String
					.format("DELETE FROM %s WHERE %s = ?", TABLE_ACTING, COL_ACTOR_ID));
			/**************************************************************************************/
			mInsertMovie = mConnection.prepareStatement(String
					.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
							TABLE_MOVIE, COL_NAME, COL_YEAR, COL_RATING, COL_VOTES));
			mInsertActor = mConnection.prepareStatement(String
					.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
							TABLE_ACTOR, COL_ACTOR_NAME, COL_ACTOR_SURNAME));
			mInsertActing = mConnection.prepareStatement(String
					.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
							TABLE_ACTING, COL_MOVIE_ID, COL_ACTOR_ID));
			mInsertGenreByMovieAndGenre = mConnection.prepareStatement(String
					.format("INSERT INTO %s (%s, %s) VALUES (?, ?)",
							TABLE_GENRE, COL_MOVIE_ID, COL_GENRE));
			/**************************************************************************************/
			mSelectMovieByName = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE LOWER(%s) LIKE ?", TABLE_MOVIE,
							COL_NAME));
			mSelectActorByNameOrSurname = mConnection.prepareStatement(String
					.format("SELECT * FROM %s WHERE (LOWER(%s) || ' ' || LOWER(%s)) LIKE ?",
							TABLE_ACTOR, COL_ACTOR_NAME, COL_ACTOR_SURNAME));
			mSelectActorByNameOrSurnameAndMovieId = mConnection.prepareStatement(String.format(
					"SELECT * FROM %s WHERE (LOWER(%s) || ' ' || LOWER(%s)) LIKE ? " +
							"AND %s IN (SELECT %s FROM %s WHERE %s = ?)",
					TABLE_ACTOR, COL_ACTOR_NAME, COL_ACTOR_SURNAME, COL_ID, COL_ACTOR_ID,
					TABLE_ACTING, COL_MOVIE_ID));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Actor> selectActors(int limit, int from) {
		List<Actor> actors = new ArrayList<>();
		try {
			ResultSet results;
			if (limit != 0) {
				mSelectActorInterval.setInt(1, limit);
				mSelectActorInterval.setInt(2, from);
				results = mSelectActorInterval.executeQuery();
			} else {
				results = mSelectActors.executeQuery();
			}
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_ACTOR_NAME);
			int surnameCol = results.findColumn(COL_ACTOR_SURNAME);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				String surname = results.getString(surnameCol);
				actors.add(new Actor(id, name, surname));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return actors;
	}

	// ToDo transaction add actor and add to movie
	public List<Actor> selectActorsInMovie(Movie movie, int limit, int from) {
		List<Actor> actors = new ArrayList<>();
		try {
			// Select actors by the movie ids
			mSelectActorIntervalByMovieId.setInt(1, movie.id);
			mSelectActorIntervalByMovieId.setInt(2, limit);
			mSelectActorIntervalByMovieId.setInt(3, from);
			ResultSet results = mSelectActorIntervalByMovieId.executeQuery();
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_ACTOR_NAME);
			int surnameCol = results.findColumn(COL_ACTOR_SURNAME);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				String surname = results.getString(surnameCol);
				actors.add(new Actor(id, name, surname));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return actors;
	}

	public List<Movie> selectMovies(int limit, int from) {
		List<Movie> movies = new ArrayList<>();
		try {
			mSelectMovieInterval.setInt(1, limit);
			mSelectMovieInterval.setInt(2, from);
			ResultSet results = mSelectMovieInterval.executeQuery();
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_NAME);
			int yearCol = results.findColumn(COL_YEAR);
			int ratingCol = results.findColumn(COL_RATING);
			int votesCol = results.findColumn(COL_VOTES);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				int year = results.getInt(yearCol);
				double rating = results.getDouble(ratingCol);
				int votes = results.getInt(votesCol);

				movies.add(new Movie(id, name, year, rating, votes));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movies;
	}

	public Movie selectMovie(int id) {
		Movie movie = null;
		try {
			mSelectMovieById.setInt(1, id);
			ResultSet results = mSelectMovieById.executeQuery();
			int nameCol = results.findColumn(COL_NAME);
			int yearCol = results.findColumn(COL_YEAR);
			int ratingCol = results.findColumn(COL_RATING);
			int votesCol = results.findColumn(COL_VOTES);
			if (results.next()) {
				String name = results.getString(nameCol);
				int year = results.getInt(yearCol);
				double rating = results.getDouble(ratingCol);
				int votes = results.getInt(votesCol);

				movie = new Movie(id, name, year, rating, votes);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movie;
	}

	public Actor selectActor(int id) {
		Actor actor = null;
		try {
			mSelectActorById.setInt(1, id);
			ResultSet results = mSelectActorById.executeQuery();
			int nameCol = results.findColumn(COL_ACTOR_NAME);
			int surnameCol = results.findColumn(COL_ACTOR_SURNAME);
			if (results.next()) {
				actor = new Actor(id, results.getString(nameCol), results.getString(surnameCol));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return actor;
	}

	public List<String> selectGenres(int movieId) {
		List<String> genres = new ArrayList<>();
		try {
			mSelectGenresByMovieId.setInt(1, movieId);
			ResultSet results = mSelectGenresByMovieId.executeQuery();
			int genreCol = results.findColumn(COL_GENRE);
			while (results.next()) {
				genres.add(results.getString(genreCol));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genres;
	}

	public void updateMovie(Movie movie) {
		try {
			mUpdateMovieById.setString(1, movie.name);
			mUpdateMovieById.setInt(2, movie.year);
			mUpdateMovieById.setDouble(3, movie.rating);
			mUpdateMovieById.setInt(4, movie.votes);
			mUpdateMovieById.setInt(5, movie.id);
			mUpdateMovieById.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateActor(Actor actor) {
		try {
			mUpdateActorById.setString(1, actor.name);
			mUpdateActorById.setString(2, actor.surname);
			mUpdateActorById.setInt(3, actor.id);
			mUpdateActorById.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean deleteMovie(Movie movie) {
		try {
			// Delete movie
			mDeleteMovieById.setInt(1, movie.id);
			mDeleteMovieById.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteActor(Actor actor) {
		try {
			// Delete actor
			mDeleteActorById.setInt(1, actor.id);
			mDeleteActorById.execute();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void insertMovie(Movie movie) {
		try {
			// Insert movie
			mInsertMovie.setString(1, movie.name);
			mInsertMovie.setInt(2, movie.year);
			mInsertMovie.setDouble(3, movie.rating);
			mInsertMovie.setInt(4, movie.votes);
			mInsertMovie.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean insertMovieWithActors(Movie movie, List<Actor> actors) {
		try {
			// Disable auto commit
			mConnection.setAutoCommit(false);
			// Insert movie
			insertMovie(movie);

			if (actors.size() > 0) {
				// Get the inserted movie's id
				mSelectMovieByName.setString(1, movie.name.toLowerCase());
				ResultSet result = mSelectMovieByName.executeQuery();
				if (result.next()) {
					int idCol = result.findColumn(COL_ID);
					movie.id = result.getInt(idCol);

					// Add actors of this movie
					for (Actor actor : actors) {
						insertActing(movie, actor);
					}
				} else {
					throw new SQLException("Finding the new movie id failed!");
				}
			}
			// Commit transaction
			mConnection.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				mConnection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return false;
	}

	public void insertActor(Actor actor) {
		try {
			// Insert actor
			mInsertMovie.setString(1, actor.name);
			mInsertMovie.setString(2, actor.surname);
			mInsertMovie.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertActing(Movie movie, Actor actor) throws SQLException {
		// Insert acting
		mInsertActing.setInt(1, movie.id);
		mInsertActing.setInt(2, actor.id);
		mInsertActing.execute();
	}

//	public void insertGenre(Movie movie, Genre genre) {
//		try {
//			// Insert genre
//			mInsertMovie.setInt(1, movie.id);
//			mInsertMovie.setString(2, genre.name);
//			mInsertMovie.execute();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

	public List<Movie> findMovies(String query) {
		List<Movie> movies = new ArrayList<>();
		try {
			// %query%, - % stands for any character sequence
			query = "%" + query.toLowerCase() + "%";
			mSelectMovieByName.setString(1, query);
			ResultSet results = mSelectMovieByName.executeQuery();
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_NAME);
			int yearCol = results.findColumn(COL_YEAR);
			int ratingCol = results.findColumn(COL_RATING);
			int votesCol = results.findColumn(COL_VOTES);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				int year = results.getInt(yearCol);
				double rating = results.getDouble(ratingCol);
				int votes = results.getInt(votesCol);
				movies.add(new Movie(id, name, year, rating, votes));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movies;
	}

	public List<Actor> findActors(String query) {
		List<Actor> actors = new ArrayList<>();
		try {
			// %query%, - % stands for any character sequence
			query = "%" + query.toLowerCase() + "%";
			mSelectActorByNameOrSurname.setString(1, query);
			ResultSet results = mSelectActorByNameOrSurname.executeQuery();
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_ACTOR_NAME);
			int surnameCol = results.findColumn(COL_ACTOR_SURNAME);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				String surname = results.getString(surnameCol);
				actors.add(new Actor(id, name, surname));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return actors;
	}

	public List<Actor> findActorsInMovie(Movie movie, String query) {
		List<Actor> actors = new ArrayList<>();
		try {
			// %query%, - % stands for any character sequence
			query = "%" + query.toLowerCase() + "%";
			mSelectActorByNameOrSurnameAndMovieId.setString(1, query);
			mSelectActorByNameOrSurnameAndMovieId.setInt(2, movie.id);
			ResultSet results = mSelectActorByNameOrSurnameAndMovieId.executeQuery();
			int idCol = results.findColumn(COL_ID);
			int nameCol = results.findColumn(COL_ACTOR_NAME);
			int surnameCol = results.findColumn(COL_ACTOR_SURNAME);
			while (results.next()) {
				int id = results.getInt(idCol);
				String name = results.getString(nameCol);
				String surname = results.getString(surnameCol);
				actors.add(new Actor(id, name, surname));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return actors;
	}

	private void close() {
		try {
			mConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static class Movie {
		int year, votes, id;
		double rating;
		String name;
		List<String> genres = new ArrayList<>();
		List<Actor> actors = new ArrayList<>();

		public Movie(int id, String name, int year, double rating, int votes) {
			this.id = id;
			this.name = name;
			this.year = year;
			this.rating = rating;
			this.votes = votes;
		}

		public Movie(String name, int year, double rating, int votes) {
			this.name = name;
			this.year = year;
			this.rating = rating;
			this.votes = votes;
		}

		@Override
		public String toString() {
			String output = name + '\t' + year + '\t' + rating + '\t' + votes + "\n\t";
			// Parse genres
			for (String genre: genres) {
				output += genre + '\t';
			}
			output = output.substring(0, output.length() - 1) + '\n';

			// Parse actors
			for (Actor actor : actors) {
				output += actor.name + " " + actor.surname + '\t';
			}
			output = output.substring(0, output.length() - 1);
			return output;
		}
	}

	public static class Actor {
		int id;
		String name, surname;

		public Actor(int id, String name, String surname) {
			this.id = id;
			this.name = name;
			this.surname = surname;
		}

		@Override
		public String toString() {
			return name + " " + surname;
		}
	}

}
