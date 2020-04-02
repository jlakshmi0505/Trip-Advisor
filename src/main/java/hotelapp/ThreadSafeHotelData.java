package hotelapp;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class ThreadSafeHotelData - extends class HotelData (rename your class from project 1 as needed).
 * Thread-safe, uses ReentrantReadWriteLock to synchronize access to all data structures.
 */
public class ThreadSafeHotelData extends HotelData {
private Map<Integer, List<Review>> tempMap;
	private ReentrantReadWriteLock lock;

	/**
	 * Default constructor.
	 */
	public ThreadSafeHotelData() {
		super();
		this.lock = new ReentrantReadWriteLock();
		this.tempMap = new HashMap<>();
	}

	/**
	 * Overrides addHotel method from HotelData class to make it thread-safe; uses the lock.
	 * Create a Hotel given the parameters, and add it to the appropriate data
	 * structure(s).
	 * 
	 * @param hotelId
	 *            - the id of the hotel
	 * @param hotelName
	 *            - the name of the hotel
	 * @param city
	 *            - the city where the hotel is located
	 * @param state
	 *            - the state where the hotel is located.
	 * @param streetAddress
	 *            - the building number and the street
	 * @param lat latitude
	 * @param lon longitude
	 */
	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, int avgRating, double lat,
						 double lon) {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		try {
			lock.writeLock().lock();
			super.addHotel(hotelId, hotelName, city, state, streetAddress, avgRating, lat, lon);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Overrides addReview method from HotelData class to make it thread-safe; uses the lock.
	 *
	 * @param hotelId
	 *            - the id of the hotel reviewed
	 * @param reviewId
	 *            - the id of the review
	 * @param rating
	 *            - integer rating 1-5.
	 * @param reviewTitle
	 *            - the title of the review
	 * @param review
	 *            - text of the review
	 * @param isRecom
	 *            - whether the user recommends it or not
	 * @param date
	 *            - date of the review
	 * @param username
	 *            - the nickname of the user writing the review.
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle the following exceptions:
	 *         ParseException if the date is invalid InvalidRatingException if
	 *         the rating is out of range
	 */
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
			boolean isRecom, String date, String username) throws ParseException {
		try {
			//Adding Review to the temp Map
			Review review1 = new Review(Integer.parseInt(hotelId), reviewId, rating, reviewTitle, review, username, isRecom, date);
			if (!tempMap.containsKey(Integer.parseInt(hotelId))) {
				List<Review> list = new ArrayList<>();
				list.add(review1);
				tempMap.put(Integer.parseInt(hotelId), list);
			} else {
				List<Review> list = tempMap.get(Integer.parseInt(hotelId));
				list.add(review1);
			}
		}
		catch (IllegalArgumentException e) {
			return false;
		}
		return false;
	}


	/** Combining the local hotelData to  the "main" ThreadSafeHotelData
	 * Iterating over the temp map and adding reviews to the main HotelData
	 * @param localData ThreadSafeHotelData
	 */
	public void addAll(ThreadSafeHotelData localData) {
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		try {
			lock.writeLock().lock();
			for (Integer hotelId : localData.tempMap.keySet()) {
				List<Review> list = localData.tempMap.get(hotelId);
				for (Review review : list) {
					try {
						super.addReview(String.valueOf(hotelId), review.getReviewId(), review.getRatingOverall(), review.getTitle(), review.getReviewText(), review.isRecommended(), review.getReviewSubmissionTime(), review.getUserNickname());
					} catch (ParseException | InvalidRatingException e) {
						e.printStackTrace();
					}
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

}
