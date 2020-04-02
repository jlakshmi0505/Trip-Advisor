package hotelapp;
import DatabaseHandler.DatabaseUtil;
import DatabaseHandler.LoginDatabaseHandler;
import DatabaseHandler.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.ToDoubleBiFunction;

/**
 *  This class will populate data for hotel details and hotel reviews
 */
public class HotelData {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private DateFormat format = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    LoginDatabaseHandler db = LoginDatabaseHandler.getInstance();


    /**
     * This method will create a Hotel with the given  parameters, and add it to the appropriate table
     * @param hotelId Hotel Id
     * @param hotelName Hotel Name
     * @param city  City
     * @param state  State
     * @param streetAddress Street Address of hotel
     * @param lat Latitude
     * @param lon Longitude
     */
    public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, int avgRating, double lat,
                         double lon) {
        Hotel hotel = new Hotel(hotelId, hotelName, city, state, streetAddress, avgRating,lat, lon);
        db.insertHotelDataIntoDB(hotel);
    }

    /**
     * This method will create a Review with the given  parameters, and add it to the appropriate table
     * @param hotelId  Hotel Id
     * @param reviewId Review Id
     * @param rating Rating that should be between 1-5
     * @param reviewTitle Review Title
     * @param review ReviewText
     * @param isRecom Is hotel Recommended by the user
     * @param date Review Submission Date
     * @param username the nickname of the user writing the review.
     * @return true if successful, false if unsuccessful because of invalid date
     * or rating. Needs to catch and handle the following exceptions:
     * ParseException if the date is invalid InvalidRatingException if
     * the rating is out of range
     */
    public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
                             boolean isRecom, String date, String username) throws ParseException, InvalidRatingException {
        System.out.println("inside");
        // Check if rating is valid
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new InvalidRatingException("Invalid Rating " + rating);
        }
        // Check if Date is valid
        if (isValidDate(date)) {
            System.out.println("outside");
            Review review1 = new Review(Integer.parseInt(hotelId), reviewId, rating, reviewTitle, review, username, isRecom, date);
            db.insertReviewDataIntoDB(review1);
            return true;
        }
        return false;
    }

    /**
     * This method will add review from the UI
     * @param hotelId
     * @param rating
     * @param reviewTitle
     * @param review
     * @param username
     * @return
     */
    public boolean addNewReview(String hotelId, int rating, String reviewTitle, String review, String username) {
        boolean isRecom = false;
        if (rating > 3) {
            isRecom = true;
        }
        Date d = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String date = format.format(d);
        if (isValidDate(date)) {
            Review review1 = new Review(Integer.parseInt(hotelId), username + hotelId, rating, reviewTitle, review, username, isRecom, date);
            db.insertReviewDataIntoDB(review1);
            return true;
        }
        return false;
    }

    /**
     * This method will edit review and call database update method
     * @param reviewId
     * @param rating
     * @param reviewTitle
     * @param reviewText
     * @return boolean value
     * @throws InvalidRatingException
     */
    public boolean editReview(String reviewId, int rating, String reviewTitle, String reviewText) throws InvalidRatingException {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new InvalidRatingException("Invalid Rating " + rating);
        }
        boolean isRecom = false;
        if (rating > 3) {
            isRecom = true;
        }
        Date d = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String date = format.format(d);
        if (isValidDate(date)) {
            db.updateReviewDataIntoDB(reviewId, rating, reviewTitle, reviewText, isRecom, date);
            return true;
        }
        return false;
    }

    /**
     * This method will delete the review from the database
     * @param reviewId
     * @return
     */
    public Status delReview(String reviewId) {
        Status st = db.delReview(reviewId);
        return st;

    }

    /**
     * This method will update rating in database
     * @param hotelId
     * @param rating
     */
    public void updateRatingInHotel(int hotelId,int rating) {
        db.updateRatingInHotel(hotelId,rating);
    }

    /** This method will check if date is valid
     * @param date Date
     * @return true if date is valid otherwise false
     */
    private boolean isValidDate(String date) {
        try {
            format.parse(date);
        } catch (ParseException e) {
            System.out.println("Invalid Date" + date);
            return false;
        }
        return true;
    }

    /**
     * Checking id is valid Integer or not
     * @param id hotel id passed by the user
     * @return int value of id
     */
    public int isInteger(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException ex) {
            System.out.println("Please provide valid hotelId  ");
            return 0;
        }
    }
}


