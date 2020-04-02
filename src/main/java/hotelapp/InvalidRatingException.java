package hotelapp;

/**
 * This exception will be thrown if rating is not in the range of [1-5]
 */
class InvalidRatingException extends Exception {

    InvalidRatingException(String errorString){
        super(errorString);
    }
}
