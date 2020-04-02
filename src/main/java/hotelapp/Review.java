package hotelapp;



/**
 * Review class that represents a review that has hotelId,reviewId,title,ratingOverall.
 */
public class Review implements Comparable<Review>{
    private int hotelId;
    private String reviewId;
    private int ratingOverall;
    private String title;
    private String reviewText;
    private String userNickname;
    private boolean isRecommended;
    private String reviewSubmissionTime;


    public Review(int hotelId, String reviewId, int ratingOverall, String title, String reviewText, String userNickname, boolean isRecommended, String reviewSubmissionTime) {
        this.hotelId = hotelId;
        this.reviewId = reviewId;
        this.ratingOverall = ratingOverall;
        this.title = title;
        this.reviewText = reviewText;
        this.userNickname = userNickname;
        this.isRecommended = isRecommended;
        this.reviewSubmissionTime = reviewSubmissionTime;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    @Override
    public String toString() {
        return System.lineSeparator() + " HotelId=" + hotelId + System.lineSeparator() +
                " ReviewId=" + reviewId + System.lineSeparator() +
                " RatingOverall=" + ratingOverall + System.lineSeparator() +
                " Title=" + title + System.lineSeparator() +
                " ReviewText=" + reviewText + System.lineSeparator() +
                " isRecom=" + isRecommended + System.lineSeparator() +
                " UserNickname=" + userNickname + System.lineSeparator() +
                " ReviewSubmissionTime=" + reviewSubmissionTime;
    }

    /** This method will sort reviews on the basis of reviewSubmissionTime...latest will be the first
     * if dates are same for two review then it will compare on the basis of userNickName if userNickName is also
     * same for two reviews then it will sort according to the reviewId...
     * @param o object of another review
     * @return int value
     */
    @Override
    public int compareTo(Review o) {
        if (o.reviewSubmissionTime.compareTo(reviewSubmissionTime) < 0) {
            return -1;
        } else if (o.reviewSubmissionTime.compareTo(reviewSubmissionTime) > 0) {
            return 1;
            //check for username
        } else if (userNickname.compareTo(o.getUserNickname()) < 0) {
            return -1;
        } else if (userNickname.compareTo(o.getUserNickname()) > 0) {
            return 1;
            // if username is also same then check with review id
        } else if (reviewId.compareTo(o.getReviewId()) < 0) {
            return -1;
        } else if (reviewId.compareTo(o.getReviewId()) > 0) {
            return 1;
        }
        return reviewId.compareTo(o.getReviewId());
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public int getRatingOverall() {
        return ratingOverall;
    }

    public String getTitle() {
        return title;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public String getReviewSubmissionTime() {
        return reviewSubmissionTime;
    }
}

