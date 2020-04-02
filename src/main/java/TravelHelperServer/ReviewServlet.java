package TravelHelperServer;
import DatabaseHandler.Status;
import hotelapp.Hotel;
import hotelapp.ThreadSafeHotelData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ReviewServlet extends TravelHelperBaseServlet {
    ThreadSafeHotelData hotelData;

    public ReviewServlet(ThreadSafeHotelData hotelData) {
        this.hotelData = hotelData;
    }

    /**
     * This method will be used for adding  reviews,editing reviews and deleting
     * @param request
     * @param response
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepareResponse("HotelDetails", response);
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            String reviewTitle = request.getParameter("reviewTitle");
            String reviewText = request.getParameter("reviewText");
            String hotelId = request.getParameter("hotelId");
            String rating = request.getParameter("rating");
            String editReview = request.getParameter("edit");
            String delReview = request.getParameter("del");
            if (editReview == null && delReview == null) {
                //new record
                addNewReview(response, user, reviewTitle, reviewText, hotelId, rating);
            } else if (editReview != null) {
                editReview(response, user, reviewTitle, reviewText, hotelId, rating);
            } else if (delReview != null) {
                deleteReview(response, user, hotelId);
            }
        } else {
            response.sendRedirect("/login.html");
        }
    }

    /**
     * This method is used for deleting review
     * @param response
     * @param user
     * @param hotelId
     */
    private void deleteReview(HttpServletResponse response, String user, String hotelId) {
        try {
            String reviewId = user + hotelId;
            Status status = hotelData.delReview(reviewId);
            if (status == Status.OK)
                System.out.println("Review deleted");
            response.sendRedirect(response.encodeRedirectURL("/hotel?hotelId=" + hotelId));
        } catch (Exception ex) {
        }
    }

    private void editReview(HttpServletResponse response, String user, String reviewTitle, String reviewText, String hotelId, String rating) {
        //edit record
        try {
            String reviewId = user + hotelId;
            boolean isEdited = hotelData.editReview(reviewId, Integer.parseInt(rating), reviewTitle, reviewText);
            if (isEdited)
                System.out.println("Review edited");
            response.sendRedirect(response.encodeRedirectURL("/hotel?hotelId=" + hotelId));
        } catch (Exception ex) {
        }
    }

    private void addNewReview(HttpServletResponse response, String user, String reviewTitle, String reviewText, String hotelId, String rating) {
        try { hotelData.addNewReview(hotelId, Integer.parseInt(rating), reviewTitle, reviewText, user);
            response.sendRedirect(response.encodeRedirectURL("/hotel?hotelId=" + hotelId));
        } catch (Exception ex) {
        }
    }
}
