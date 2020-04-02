package DatabaseHandler;

import hotelapp.Hotel;
import hotelapp.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Random;


public class LoginDatabaseHandler {


	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();

	/** Used to determine if necessary tables are provided. */
	private static final String USER_TBL_SQL =
	        "SHOW TABLES LIKE 'login_users';";
	private static final String HOTEL_TBL_SQL =
			"SHOW TABLES LIKE 'hotel_details';";
	private static final String REVIEW_TBL_SQL =
			"SHOW TABLES LIKE 'review_details';";
	private static final String SAVE_HOTEL_SQL =
			"SHOW TABLES LIKE 'saved_hotel_details';";
	private static final String VISITED_HOTEL_SQL =
			"SHOW TABLES LIKE 'visited_hotel_details';";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL =
			"CREATE TABLE login_users (" +
			"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
			"username VARCHAR(32) NOT NULL UNIQUE, " +
			"password CHAR(64) NOT NULL, " +
			"usersalt CHAR(32) NOT NULL,"+
					"lastLoginDate date NOT NULL,"+
					"lastLoginTime time NOT NULL);";

	private static final String CREATE_HOTEL_DB =
			"CREATE TABLE hotel_details (" +
					"hotelId INTEGER PRIMARY KEY, " +
					"hotelName VARCHAR(64) NOT NULL UNIQUE, " +
					"avgRating INTEGER , " +
					"latitude DOUBLE NOT NULL, " +
					"longitude DOUBLE NOT NULL, " +
					"street CHAR(64) NOT NULL, " +
					"city CHAR(64) NOT NULL, " +
					"state CHAR(32) NOT NULL);";
	private static final String CREATE_REVIEW_DB =
			"CREATE TABLE review_details (" +
					"reviewId VARCHAR(64) PRIMARY KEY, " +
					"hotelId INTEGER NOT NULL, " +
					"rating INTEGER NOT NULL, " +
					"title VARCHAR(64) NOT NULL, " +
					"reviewText TEXT NOT NULL, " +
					"userNickname VARCHAR(64) NOT NULL, " +
					"userId INTEGER, " +
					"isRecommended VARCHAR(32) NOT NULL, " +
					"reviewSubmissionTime CHAR(64) NOT NULL);";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_SAVE_HOTEL_SQL =
			"CREATE TABLE saved_hotel_details (" +
					"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL, " +
					"hotelName VARCHAR(64) NOT NULL," +
					"hotelId INTEGER NOT NULL," +
					"UNIQUE KEY `unique_key`(`username`,`hotelId`));";

	/** Used to create necessary tables for this example. */
	private static final String CREATE_VISITED_HOTEL_SQL =
			"CREATE TABLE visited_hotel_details (" +
					"id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
					"username VARCHAR(32) NOT NULL, " +
					"hotelName VARCHAR(64) NOT NULL, " +
					"hotelId INTEGER NOT NULL, " +
					"UNIQUE KEY `unique_key`(`username`,`hotelId`));";


	/** Used to insert a  hotel details into the database. */
	private static final String INSERT_HOTEL_SQL =
			"INSERT INTO hotel_details (hotelId, hotelName,avgRating,latitude,longitude,street,city,state) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

	/** Used to insert a review details into the database. */
	private static final String INSERT_REVIEW_SQL =
			"INSERT INTO review_details (reviewId,hotelId,rating,title,reviewText,userNickname,userId,isRecommended,reviewSubmissionTime)" +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

	/** Used to update hotel details into the database. */
	private static final String UPDATE_REVIEW_SQL =
			"UPDATE review_details SET rating = ? ,title = ?,reviewText = ?,isRecommended = ?, reviewSubmissionTime=? WHERE reviewId = ?";

	/** Used to delete review from the database. */
	private static final String DELETE_REVIEW =
			"DELETE FROM review_details WHERE reviewId = ?";

	/** Used to retrieve hotel details from the database. */
	private static final String SELECT_HOTEL_SQL =
			"SELECT * FROM hotel_details";

	/** Used to retrieve review details from the database. */
	private static final String SELECT_REVIEW_SQL =
			"SELECT * FROM review_details";

	/** Used to update rating to the database. */
	private static final String UPDATE_RATING_SQL =
			"UPDATE hotel_details SET AVGRATING = ? WHERE HOTELID = ?";

	/** Used to configure connection to database. */
	private DatabaseConnector db;

	/** Used to generate password hash salt for user. */
	private Random random;

	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private LoginDatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());
		try {
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		}
		catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		}
		catch (IOException e) {
			status = Status.MISSING_VALUES;
		}

		if (status != Status.OK) {
		}
	}

	/**
	 * Gets the single instance of the database handler.
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance() {
		return singleton;
	}


	/**
	 * Checking whether hotel table is already populated
	 * @return boolean value
	 */
	public boolean checkHotelTableAlreadyPopulated() {
		try (Connection connection = db.getConnection();) {
			PreparedStatement statement = connection.prepareStatement(SELECT_HOTEL_SQL);
			ResultSet rs = statement.executeQuery();
			if (rs.next() == false) {
				return false;
			}
		} catch (SQLException ex) {
		}
		return true;
	}

	/**
	 * Checking whether review table is already populated
	 * @return boolean value
	 */
	public boolean checkReviewTableAlreadyPopulated() {
		try (Connection connection = db.getConnection();) {
			PreparedStatement statement = connection.prepareStatement(SELECT_REVIEW_SQL);
			ResultSet rs = statement.executeQuery();
			if(rs.next() == false){
				return false;
			}
		} catch (SQLException ex) {

		}
		return true;
	}

	/**
	 * Updating rating in hotel table
	 */
	public void updateRatingInHotel(int hotelId,int rating){
		try (
				Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement(UPDATE_RATING_SQL);
		) {
			statement.setInt(1,rating);
			statement.setInt(2,hotelId);
			int row = statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	/**
	 * Checks if necessary table exists in database, and if not tries to
	 * create it.
	 */
	protected Status setupTables() {
		Status status = Status.ERROR;
		try (Connection connection = db.getConnection();
			 Statement statement = connection.createStatement();) {
			if (!statement.executeQuery(USER_TBL_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_SQL);
				// Check if create was successful
			}
			if (!statement.executeQuery(HOTEL_TBL_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_HOTEL_DB);
				// Check if create was successful
			}
			if (!statement.executeQuery(REVIEW_TBL_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_REVIEW_DB);
				// Check if create was successful
			}
			if (!statement.executeQuery(SAVE_HOTEL_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_SAVE_HOTEL_SQL);
				// Check if create was successful
			}
			if (!statement.executeQuery(VISITED_HOTEL_SQL).next()) {
				// Table missing, must create
				statement.executeUpdate(CREATE_VISITED_HOTEL_SQL);
				// Check if create was successful
			}
			if ((!statement.executeQuery(USER_TBL_SQL).next()) || (!statement.executeQuery(HOTEL_TBL_SQL).next()) ||
					(!statement.executeQuery(REVIEW_TBL_SQL).next()) || (!statement.executeQuery(SAVE_HOTEL_SQL).next()) ||
					(!statement.executeQuery(VISITED_HOTEL_SQL).next())) {
				status = Status.CREATE_FAILED;
			} else {
				status = Status.OK;
			}
		} catch (Exception ex) {
			status = Status.CREATE_FAILED;
		}
		return status;
	}

	/**
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 */
	public Status insertHotelDataIntoDB(Hotel hotel) {
		Status status = Status.ERROR;
		try (
				Connection connection = db.getConnection();
		) {
			status = insertHotelDataIntoDB(connection, hotel);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
		}

		return status;
	}

	/**
	 * Inserting  hotel data in hotel table
	 * @return Status
	 */
	private Status insertHotelDataIntoDB(Connection connection, Hotel hotel) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_SQL);
		) {
			statement.setString(1, hotel.getId());
			statement.setString(2, hotel.getF());
			statement.setString(3, "0");
			statement.setDouble(4, hotel.getLat());
			statement.setDouble(5, hotel.getLng());
			statement.setString(6, hotel.getAd());
			statement.setString(7, hotel.getCi());
			statement.setString(8, hotel.getPr());
			statement.executeUpdate();
			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
		}
		return status;
	}

	/**
	 * Inserting  review data in hotel table
	 * @return Status
	 */
	public Status insertReviewDataIntoDB(Review review) {
		Status status = Status.ERROR;
		try (Connection connection = db.getConnection();
		) {
			status = insertReviewDataIntoDB(connection, review);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
		}
		return status;
	}


	/**
	 * Inserting  review data in hotel table
	 * @return Status
	 */
	private Status insertReviewDataIntoDB(Connection connection, Review review) {
		Status status = Status.ERROR;
		try (PreparedStatement statement = connection.prepareStatement(INSERT_REVIEW_SQL);
		) {
			statement.setString(1, review.getReviewId());
			statement.setInt(2, review.getHotelId());
			statement.setInt(3, review.getRatingOverall());
			statement.setString(4, review.getTitle());
			statement.setString(5, review.getReviewText());
			String userName = review.getUserNickname();
			if(userName != null && userName.length() == 0)
				userName = "Anonymous";
			statement.setString(6, userName);
			statement.setString(7, "0");
			statement.setString(8, String.valueOf(review.isRecommended()));
			statement.setString(9, review.getReviewSubmissionTime());
			statement.executeUpdate();
			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
		}
		return status;
	}


	/**
	 * Update  review data into database
	 * @return Status
	 */
	public Status updateReviewDataIntoDB(String reviewId,int rating,String reviewTitle,String reviewText,boolean isRecom,String date) {
		Status status = Status.ERROR;
		try (
				Connection connection = db.getConnection();
		) {
			status = updateReviewDataIntoDB(connection, reviewId,rating,reviewTitle,reviewText,isRecom,date);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
		}
		return status;
	}


	/**
	 * Update  review data into database
	 * @return Status
	 */
	private Status updateReviewDataIntoDB(Connection connection, String reviewId, int rating, String reviewTitle, String reviewText, boolean isRecom, String date) {
		Status status = Status.ERROR;
		try (
				PreparedStatement statement = connection.prepareStatement(UPDATE_REVIEW_SQL);
		) {
			statement.setInt(1, rating);
			statement.setString(2, reviewTitle);
			statement.setString(3, reviewText);
			statement.setString(4, String.valueOf(isRecom));
			statement.setString(5, date);
			statement.setString(6, reviewId);
			statement.executeUpdate();
			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.out.println(ex);
		}
		return status;
	}
	public Status delReview(String reviewId) {
		Status status = Status.ERROR;
		try (
				Connection connection = db.getConnection();
		) {
			status = delReview(connection, reviewId);
		} catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
		}
		return status;
	}

	/**
	 * Delete  review from the database
	 * @return Status
	 */

	private Status delReview(Connection connection, String reviewId) {
		Status status = Status.ERROR;
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_REVIEW);
		) {
			statement.setString(1, reviewId);
			statement.executeUpdate();
			status = Status.OK;
		} catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
		}
		return status;
	};
}
