package DatabaseHandler;


import hotelapp.Hotel;
import hotelapp.Review;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class DatabaseUtil {

    /**
     * Makes sure only one database handler is instantiated.
     */
    private static DatabaseUtil singleton = new DatabaseUtil();


    /**
     * Used to insert a new user into the database.
     */
    private static final String REGISTER_SQL =
            "INSERT INTO login_users (username,password,usersalt) " +
                    "VALUES (?, ?, ?);";

    /**
     * Used to determine if a username already exists.
     */
    private static final String USER_SQL =
            "SELECT username FROM login_users WHERE username = ?";

    /**
     * Used to retrieve the salt associated with a specific user.
     */
    private static final String SALT_SQL =
            "SELECT usersalt FROM login_users WHERE username = ?";

    private static final String CITY_SQL =
            "SELECT DISTINCT CITY FROM hotel_details";
    private static final String GET_SAVED_HOTEL_DETAILS =
            "SELECT  * FROM saved_hotel_details where username = ?";
    private static final String GET_VISITED_HOTEL_DETAILS =
            "SELECT  * FROM visited_hotel_details where username = ?";

    private static final String SELECT_CITY_SQL =
            "SELECT * FROM hotel_details where city = ?";

    private static final String SELECT_HOTEL_BY_ID_SQL =
            "SELECT * FROM hotel_details where hotelId = ?";

    private static final String SELECT_REVIEWS_BY_ID_SQL =
            "SELECT * FROM review_details where hotelId = ? order by reviewSubmissionTime desc";
    private static final String CHECK_USER_ALREADY_ADDED_REVIEW =
            "select * from review_details where hotelId = ? and userNickName= ? and userId= ?";
    private static final String UPDATE_LAST_LOGIN_SQL =
            "UPDATE login_users SET lastLoginDate = ?,lastLoginTime =? WHERE username = ?";
    private static final String SELECT_USER_LAST_LOGIN_DETAILS =
            "SELECT lastLoginDate,lastLoginTime FROM login_users where username = ?";
    private static final String INSERT_HOTEL_ID_INTO_SAVE_HOTEL_LIST =
            "INSERT INTO saved_hotel_details (username,hotelName,hotelId)" +
                    "VALUES (?, ?, ?);";
    private static final String DELETE_HOTEL_ID_FROM_SAVE_HOTEL_LIST =
    "DELETE FROM saved_hotel_details WHERE username = ?";
    private static final String INSERT_VISITED_HOTEL_ID_INTO_SAVE_HOTEL_LIST =
            "INSERT INTO visited_hotel_details (username,hotelName,hotelId)" +
                    "VALUES (?, ?, ?);";
    private static final String DELETE_VISITED_HOTEL_ID_FROM_SAVE_HOTEL_LIST =
            "DELETE FROM visited_hotel_details WHERE username = ?";
    private static final String SELECT_AVG_HOTEL_DETAILS =
            "SELECT hotelId,AVG(rating) as avgRating FROM review_details group by hotelId ";

    /**
     * Used to authenticate a user.
     */
    private static final String AUTH_SQL =
            "SELECT username FROM login_users " +
                    "WHERE username = ? AND password = ?";

    /**
     * Used to configure connection to database.
     */
    private DatabaseConnector db;

    /**
     * Used to generate password hash salt for user.
     */
    private Random random;

    /**
     * Initializes a database handler for the Login example. Private constructor
     * forces all other classes to use singleton.
     */
    public DatabaseUtil() {
        Status status = Status.OK;
        random = new Random(System.currentTimeMillis());
        try {
            db = new DatabaseConnector("database.properties");
            if (db.testConnection()) {
                status = Status.CONNECTION_FAILED;
            }
        } catch (FileNotFoundException e) {
            status = Status.MISSING_CONFIG;
        } catch (IOException e) {
            status = Status.MISSING_VALUES;
        }
    }

    /**
     * Gets the single instance of the database handler.
     * @return instance of the database handler
     */
    public static DatabaseUtil getInstance() {
        return singleton;
    }

    /**
     * Checks to see if a String is null or empty.
     *
     * @param text - String to check
     * @return true if non-null and non-empty
     */
    private static boolean isBlank(String text) {
        return (text == null) || text.trim().isEmpty();
    }


    /**
     * Tests if a user already exists in the database. Requires an active
     * database connection.
     *
     * @param connection - active database connection
     * @param user       - username to check
     * @return Status.OK if user does not exist in database
     * @throws SQLException Exception
     */
    private Status duplicateUser(Connection connection, String user) {
        Status status = Status.ERROR;
        try (PreparedStatement statement = connection.prepareStatement(USER_SQL);) {
            statement.setString(1, user);
            ResultSet results = statement.executeQuery();
            status = results.next() ? Status.DUPLICATE_USER : Status.OK;
        } catch (SQLException e) {
            //log.debug(e.getMessage(), e);
            status = Status.SQL_EXCEPTION;
        }
        return status;
    }

    /**
     * Tests if a user already exists in the database.
     *
     * @param user - username to check
     * @return Status.OK if user does not exist in database
     * @see #duplicateUser(Connection, String)
     */
    public Status duplicateUser(String user) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            status = duplicateUser(connection, user);
        } catch (SQLException e) {
            status = Status.CONNECTION_FAILED;
            //log.debug(e.getMessage(), e);
        }return status;
    }


    /**
     * Returns the hex encoding of a byte array.
     *
     * @param bytes  - byte array to encode
     * @param length - desired length of encoding
     * @return hex encoded byte array
     */
    private static String encodeHex(byte[] bytes, int length) {
        BigInteger bigint = new BigInteger(1, bytes);
        String hex = String.format("%0" + length + "X", bigint);

        assert hex.length() == length;
        return hex;
    }

    /**
     * Calculates the hash of a password and salt using SHA-256.
     *
     * @param password - password to hash
     * @param salt     - salt associated with user
     * @return hashed password
     */
    private static String getHash(String password, String salt) {
        String salted = salt + password;
        String hashed = salted;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salted.getBytes());
            hashed = encodeHex(md.digest(), 64);
        } catch (Exception ex) {
            //  log.debug("Unable to properly hash password.", ex);
        }

        return hashed;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status ok if registration successful
     */
    private Status registerUser(Connection connection, String newuser, String newpass) {
        Status status = Status.ERROR;
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String usersalt = encodeHex(saltBytes, 32);
        String passhash = getHash(newpass, usersalt);
        try (PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);) {
            statement.setString(1, newuser);
            statement.setString(2, passhash);
            statement.setString(3, usersalt);
            statement.executeUpdate();
            status = Status.OK;
        } catch (SQLException ex) {
            status = Status.SQL_EXCEPTION;
            System.out.println(ex);
        }
        return status;
    }

    /**
     * Registers a new user, placing the username, password hash, and
     * salt into the database if the username does not already exist.
     *
     * @param newuser - username of new user
     * @param newpass - password of new user
     * @return status.ok if registration successful
     */
    public Status registerUser(String newuser, String newpass) {
        Status status = Status.ERROR;
        if (isBlank(newuser) || isBlank(newpass)) {
            status = Status.INVALID_LOGIN;
            return status;
        }
        try (Connection connection = db.getConnection();) {
            status = duplicateUser(connection, newuser);
            // if okay so far, try to insert new user
            if (status == Status.OK) {
                status = registerUser(connection, newuser, newpass);
            }
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;
        }
        return status;
    }

    /**
     * Gets the salt for a specific user.
     * @param connection - active database connection
     * @param user       - which user to retrieve salt for
     * @return salt for the specified user or null if user does not exist
     * @throws SQLException if any issues with database connection
     */
    private String getSalt(Connection connection, String user) throws SQLException {
        assert connection != null;
        assert user != null;
        String salt = null;
        try (PreparedStatement statement = connection.prepareStatement(SALT_SQL);) {
            statement.setString(1, user);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                salt = results.getString("usersalt");
            }
        }
        return salt;
    }

    /**
     * Get list of cities from the hotel
     * @return list of String
     * @throws SQLException
     */
    public List<String> getCitiesFromHotel() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(CITY_SQL);) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                list.add(results.getString("city"));
            }
        }
        return list;
    }


    /**
     * Get list of save hotels by the user
     * @return list of String
     * @throws SQLException
     */
    public List<String> getListOfSavedHotel(String username) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_SAVED_HOTEL_DETAILS);) {
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                list.add(results.getString("hotelName"));
            }
        }
        return list;
    }

    /**
     * Get list of visited hotels
     * @return list of String
     * @throws SQLException
     */
    public List<String> getListOfVisitedHotel(String username) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(GET_VISITED_HOTEL_DETAILS);
        ){
            statement.setString(1,username);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                list.add(results.getString("hotelName"));
            }
        }
        return list;
    }

    /**
     * Get list of hotels by name and city
     * @return list of hotels
     */
    public List<Hotel> findHotelByNameAndCity(String name, String city) {
        List<Hotel> searchList = new ArrayList<>();
        List<Hotel> hotelList = findHotelByCity(city);
        if (name != null && name.length() > 0) {
            if (hotelList.size() > 0) {
                for (Hotel hotel : hotelList) {
                    String hotelName = hotel.getF();
                    if (hotelName.toUpperCase().contains(name.toUpperCase())) {
                        searchList.add(hotel);
                    }
                }
            }
            return searchList;
        } else {
            return hotelList;
        }
    }

    /**
     * Get Avg rating for the hotel
     * @return map with rating
     * @throws SQLException
     */
    public Map<String, Integer> selectAvgRating() {
        Map<String, Integer> hotelMap = new HashMap<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_AVG_HOTEL_DETAILS);) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String hotelId = results.getString("hotelId");
                int rating = results.getInt("avgRating");
                hotelMap.put(hotelId, rating);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotelMap;
    }

    /**
     * Get hotel by city from the database
     * @return list of String
     * @throws SQLException
     */
    private List<Hotel> findHotelByCity(String city) {
        List<Hotel> hotelList = new ArrayList<>();
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_CITY_SQL);
        ) {
            statement.setString(1, city);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String hotelId = results.getString("hotelId");
                String hotelName = results.getString("hotelName");
                String state = results.getString("state");
                String street = results.getString("street");
                int avgRating = results.getInt("avgRating");
                double lat = results.getDouble("latitude");
                double lang = results.getDouble("longitude");
                Hotel h = new Hotel(hotelId, hotelName, city, state, street, avgRating, lat, lang);
                hotelList.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hotelList;
    }

    /**
     * Get Hotel by  querying database with hotelId
     * @return list of String
     * @throws SQLException
     */
    public Hotel findHotelById(String hotelId) {
        Hotel h = null;
        try (
                Connection connection = db.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_HOTEL_BY_ID_SQL);
        ) {
            statement.setString(1, hotelId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String hotelName = results.getString("hotelName");
                String city = results.getString("city");
                String state = results.getString("state");
                String street = results.getString("street");
                int avgRating = results.getInt("avgRating");
                double lat = results.getDouble("latitude");
                double lang = results.getDouble("longitude");
                h = new Hotel(hotelId, hotelName, city, state, street, avgRating, lat, lang);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return h;
    }

    /**
     * Get reviews  by hotel Id
     * @return list of reviews
     */
    public List<Review> findReviewsByHotelId(String hotelId) {
        List<Review> list = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_REVIEWS_BY_ID_SQL);
        ) {
            statement.setString(1, hotelId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String reviewId = results.getString("reviewId");
                String title = results.getString("title");
                String reviewText = results.getString("reviewText");
                String user = results.getString("userNickName");
                int rating = results.getInt("rating");
                boolean isRecomm = results.getBoolean("isRecommended");
                String reviewSubmissionTime = results.getString("reviewSubmissionTime");
                Review review = new Review(Integer.parseInt(hotelId), reviewId, rating, title, reviewText, user, isRecomm, reviewSubmissionTime);
                list.add(review);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Requires an active database connection.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return status.ok if authentication successful
     * @throws SQLException
     */
    private Status authenticateUser(Connection connection, String username,
                                    String password) throws SQLException {
        Status status = Status.ERROR;
        try (PreparedStatement statement = connection.prepareStatement(AUTH_SQL);) {
            String usersalt = getSalt(connection, username);
            String passhash = getHash(password, usersalt);

            statement.setString(1, username);
            statement.setString(2, passhash);

            ResultSet results = statement.executeQuery();
            status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
        } catch (SQLException e) {
            status = Status.SQL_EXCEPTION;
        }
        return status;
    }

    /**
     * Checks if the provided username and password match what is stored
     * in the database. Must retrieve the salt and hash the password to
     * do the comparison.
     *
     * @param username - username to authenticate
     * @param password - password to authenticate
     * @return status.ok if authentication successful
     */
    public Status authenticateUser(String username, String password) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();){
            status = authenticateUser(connection, username, password);
        } catch (SQLException ex) {
            status = Status.CONNECTION_FAILED;

        }
        return status;
    }

    /**
     * Get  last login details of the user
     * @param username
     * @return
     */
    public String getLastLoginDetails(String username) {
        String dateTimeStr = null;
        try (Connection connection = db.getConnection();) {
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_LAST_LOGIN_DETAILS);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Date d = rs.getDate("lastLoginDate");
                Time t = rs.getTime("lastLoginTime");
                if(d !=null && t!= null)
                dateTimeStr = d.toString() + "," + t.toString();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return dateTimeStr;
    }

    /**
     * Removes a user from the database if the username and password are
     * provided correctly.
     * @param username - username to remove
     * @param lastLoginDate - last login date
     * @return Status.OK if removal successful
     */
    public void updateLastLogin(String username, LocalDate lastLoginDate, LocalTime lastLoginTime) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();){
            java.sql.Date date = java.sql.Date.valueOf(lastLoginDate);
            java.sql.Time time = java.sql.Time.valueOf(lastLoginTime);
            PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_LOGIN_SQL);
            statement.setDate(1,date);
            statement.setTime(2,time);
            statement.setString(3, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Add  hotel  saved by the user to the database
     * @param username
     * @return
     */
    public Status saveHotelSelected(String username,String hotelName,String hotelId) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();){
            PreparedStatement statement = connection.prepareStatement(INSERT_HOTEL_ID_INTO_SAVE_HOTEL_LIST);
            statement.setString(1,username);
            statement.setString(2,hotelName);
            statement.setString(3,hotelId);
            int results = statement.executeUpdate();
            status = results != 0 ? status = Status.OK : Status.ERROR;
        } catch (SQLException e) {
        }
        return status;
    }

    /**
     * Deleting saved hotel list from the database
     * @param username
     * @return
     */
    public Status deleteSavedHotelForUser(String username) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            PreparedStatement statement = connection.prepareStatement(DELETE_HOTEL_ID_FROM_SAVE_HOTEL_LIST);
            statement.setString(1, username);
            int results = statement.executeUpdate();
            status = results != 0 ? status = Status.OK : Status.ERROR;
        } catch (SQLException e) {
        }
        return status;
    }

    /**
     * Add  visited hotel link to the table
     * @param username
     * @return
     */
    public Status addHotelVisited(String username,String hotelName,String hotelId) {
        Status status = Status.ERROR;
        try (
                Connection connection = db.getConnection();
        ){
            PreparedStatement statement = connection.prepareStatement(INSERT_VISITED_HOTEL_ID_INTO_SAVE_HOTEL_LIST);
            statement.setString(1,username);
            statement.setString(2,hotelName);
            statement.setString(3,hotelId);
            int results = statement.executeUpdate();
            status = results != 0 ? status = Status.OK : Status.ERROR;
        } catch (SQLException e) {
        }
        return status;
    }

    /**
     * Delete the visited hotel link from the table
     * @param username
     * @return
     */
    public Status deleteVisitedHotelForUser(String username) {
        Status status = Status.ERROR;
        try (Connection connection = db.getConnection();) {
            PreparedStatement statement = connection.prepareStatement(DELETE_VISITED_HOTEL_ID_FROM_SAVE_HOTEL_LIST);
            statement.setString(1, username);
            int results = statement.executeUpdate();
            status = results != 0 ? status = Status.OK : Status.ERROR;
        } catch (SQLException e) {
        }
        return status;
    }

    /**
     * Checking if user already added review by queering the review table
     * @param hotelId hotel id
     * @param userName name of the user
     * @return
     */
    public boolean isUserAlreadyAddedReview(String hotelId, String userName) {
        ResultSet rs;
        PreparedStatement statement;
        try (Connection connection = db.getConnection();) {
            statement = connection.prepareStatement(CHECK_USER_ALREADY_ADDED_REVIEW);
            statement.setString(1, hotelId);
            statement.setString(2, userName);
            statement.setString(3, "0");
            rs = statement.executeQuery();
            if (rs.next() == false) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}