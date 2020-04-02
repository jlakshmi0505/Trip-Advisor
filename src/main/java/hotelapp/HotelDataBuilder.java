package hotelapp;

import DatabaseHandler.LoginDatabaseHandler;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Class HotelDataBuilder. Loads hotel info from input files to ThreadSafeHotelData (using multithreading). */
public class HotelDataBuilder {
	private static final String REVIEW_DETAILS = "reviewDetails";
	private static final String REVIEW_COLLECTION = "reviewCollection";
	private static final String REVIEW = "review";
	private ThreadSafeHotelData hdata;
	private ExecutorService executorService;
	private int numOfThreads;
	private final static Logger log = LogManager.getLogger("HotelDataBuilder.class");
	private Gson gson = new Gson();


	/** Constructor for class HotelDataBuilder that takes ThreadSafeHotelData and
	 * the number of threads to create as a parameter.
	 * @param data ThreadSafeHotelData
	 * @param numThreads no of Threads
	 */
	public HotelDataBuilder(ThreadSafeHotelData data, int numThreads) {
		this.hdata = data;
		this.numOfThreads=numThreads;
        this.executorService = Executors.newFixedThreadPool(numThreads);
	}


	/**
	 * Read the json file with information about the hotels and load it into the
	 * appropriate data structure(s).
	 * @param jsonFilename file name of Hotel json
	 */
	public void loadHotelInfo(String jsonFilename) {
		LoginDatabaseHandler db = LoginDatabaseHandler.getInstance();
		try (JsonReader jsonReader = new JsonReader(new FileReader(jsonFilename))) {
			JsonElement jsonElement = new JsonParser().parse(jsonReader).getAsJsonObject();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonElement hotelElem = jsonObject.getAsJsonObject().get("sr");
			if (hotelElem.isJsonArray()) {
				JsonArray hotelElemAsJsonArray = hotelElem.getAsJsonArray();
				for (JsonElement ht : hotelElemAsJsonArray) {
					double lat = 0;
					double lng = 0;
					JsonObject r = ht.getAsJsonObject();
					Hotel hotel = gson.fromJson(r, Hotel.class);
					if (r.get("ll").isJsonObject()) {
						lat = r.get("ll").getAsJsonObject().get("lat").getAsDouble();
						lng = r.get("ll").getAsJsonObject().get("lng").getAsDouble();
					}
					hotel.setLat(lat);
					hotel.setLng(lng);
					hdata.addHotel(hotel.getId(), hotel.getF(), hotel.getCi(), hotel.getPr(), hotel.getAd(), 0,hotel.getLat(), hotel.getLng());
				}
			}
		} catch (FileNotFoundException e) {
			log.error(" Json File not found" + jsonFilename);
		} catch (IOException e) {
			log.error(" Exception occurred while parsing Hotel Json File" + jsonFilename);
		}
	}


	/** Loads reviews from json files. Recursively processes subfolders.
	 *  Each json file with reviews should be processed concurrently (you need to create a new runnable job for each
	 *  json file that you encounter)
	 *  @param dir Path of the review file
	 */
	public void loadReviews(Path dir) {
		getReviews(dir);
	}

	/**
	 * This method  will recursively traverse the directory with reviews and process the json files
	 * @param dir  Path of directory
	 */
	private void getReviews(Path dir) {
		if (Files.isRegularFile(dir)) {
			try {
				executorService.submit(new Inner(String.valueOf(dir))).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			try (DirectoryStream<Path> filesList = Files.newDirectoryStream(dir)) {
				for (Path file : filesList) {
					if (!Files.isDirectory(file)) {
						executorService.submit(new Inner(String.valueOf(file))).get();
					} else {
						getReviews(file);
					}
				}
			} catch (IOException e) {
				log.error("Invalid directory/file: " + dir);
			} catch (InterruptedException | ExecutionException e) {
				log.error("Error happened while  review file to executor service" );
			}
		}
	}


	/**
	 *   Inner class that implements runnable and have run method which will add review to ThreadsafeHotel data
	 */
	private class Inner implements Runnable {
		private String fileName;

		Inner(String file) {
			this.fileName = file;
		}
		@Override
		public void run() {
			try {
				ThreadSafeHotelData localData = new ThreadSafeHotelData();
				parseReviewJson(fileName,localData);
				//combining all localHotelData with main threadSafeHotelData
				hdata.addAll(localData);
			} catch (ParseException e) {
				log.error("Error in Inner class while adding review to map");
			}

		}
	}


	/** This method will parse the json file of given path and will call the addReview on localData
	 * @param filePath file path of json
	 * @param threadSafeHotelData Local ThreadsafeHotelData object
	 * @throws ParseException Parse Exception
	 */
	private void parseReviewJson(String filePath,ThreadSafeHotelData threadSafeHotelData) throws ParseException {
		try (JsonReader jsonReader = new JsonReader(new FileReader(filePath))) {
			JsonElement jsonElement = new JsonParser().parse(jsonReader).getAsJsonObject();
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			JsonElement element = jsonObject.get(REVIEW_DETAILS).getAsJsonObject().get(REVIEW_COLLECTION);
			JsonElement reviewEle = element.getAsJsonObject().get(REVIEW);
			JsonElement reviewSummary = jsonObject.get(REVIEW_DETAILS).getAsJsonObject().get("reviewSummaryCollection");
			JsonElement summary = reviewSummary.getAsJsonObject().get("reviewSummary");
			if (summary.isJsonArray()) {
				JsonArray reviewEleAsJsonArray = summary.getAsJsonArray();
				int hotelId = reviewEleAsJsonArray.get(0).getAsJsonObject().get("hotelId").getAsInt();
				int avgHotelRating = reviewEleAsJsonArray.get(0).getAsJsonObject().get("avgOverallRating").getAsInt();
				hdata.updateRatingInHotel(hotelId,avgHotelRating);
			}
			if (reviewEle.isJsonArray()) {
				JsonArray reviewEleAsJsonArray = reviewEle.getAsJsonArray();
				for (JsonElement review : reviewEleAsJsonArray) {
					JsonObject r = review.getAsJsonObject();
					Review review1 = gson.fromJson(r, Review.class);
					threadSafeHotelData.addReview(String.valueOf(review1.getHotelId()), review1.getReviewId(), review1.getRatingOverall(), review1.getTitle(), review1.getReviewText(), review1.isRecommended(), review1.getReviewSubmissionTime(), review1.getUserNickname());
				}
			}
		} catch (FileNotFoundException e) {
			log.error(" Json File not found" + filePath + e);
		} catch (IOException e) {
			log.error(" Exception occurred while parsing Review Json File" + filePath + e);
		}
	}
}
