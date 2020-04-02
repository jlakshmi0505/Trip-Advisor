package TravelHelperServer;
import DatabaseHandler.DatabaseUtil;
import com.google.gson.JsonObject;
import hotelapp.Hotel;
import hotelapp.Review;
import hotelapp.ThreadSafeHotelData;
import hotelapp.TouristAttractionFinder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

public class HotelServlet extends HttpServlet {
    DatabaseUtil db = DatabaseUtil.getInstance();

    /**
     * This method will be invoked when we make get request call to jetty server with url /hotel
     * Here query parameter will be validated like hotelId
     * and if that is not present in the parameter then error json response will be send to the browser
     * @param request  HttpRequest
     * @param response HttpServletResponse
     * @throws IOException Exception
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            ve.init();
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("resource/hotelPage.html");
            StringWriter writer = new StringWriter();
            String hotelId = request.getParameter("hotelId");
            if (hotelId != null) {
                hotelId = StringEscapeUtils.escapeHtml4(hotelId);
                boolean isAlreadyAddedReview = db.isUserAlreadyAddedReview(hotelId, user);
                Hotel hotel = db.findHotelById(hotelId);
                appendMessage(out, user, context, template, writer, hotelId, isAlreadyAddedReview, hotel);
            }
        } else {
            response.sendRedirect("/login.html");
        }
    }

    private void appendMessage(PrintWriter out, String user, VelocityContext context, Template template, StringWriter writer, String hotelId, boolean isAlreadyAddedReview, Hotel hotel) {
        List<Review> listOfReviews = db.findReviewsByHotelId(hotelId);
        context.put("user", user);
        context.put("isAlreadyAddedReview", isAlreadyAddedReview);
        context.put("hotel", hotel);
        context.put("listOfReviews", listOfReviews);
        template.merge(context, writer);
        out.println(writer.toString());
    }
}