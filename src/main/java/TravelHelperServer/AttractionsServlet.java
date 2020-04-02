package TravelHelperServer;

import DatabaseHandler.DatabaseUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hotelapp.Hotel;
import hotelapp.ThreadSafeHotelData;
import hotelapp.TouristAttractionFinder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.app.VelocityEngine;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AttractionsServlet extends HttpServlet {
    private ThreadSafeHotelData data;
    DatabaseUtil db = DatabaseUtil.getInstance();

    public AttractionsServlet(ThreadSafeHotelData data) {
        super();
        this.data = data;
    }

    /** This method will be invoked when we make get request call to jetty server
     * Here query parameter will be validated like hotelId and radius
     * and if any of them is not present in the parameter then error json response will be send
     * @param request HttpRequest
     * @param response HttpServletResponse
     * @throws IOException Exception
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            ve.init();
            String hotelId = request.getParameter("hotelId");
            String radius = request.getParameter("radius");
            hotelId = StringEscapeUtils.escapeHtml4(hotelId);
            TouristAttractionFinder finder = new TouristAttractionFinder(data);
            Hotel hotel = db.findHotelById(hotelId);
            List<String> attractionName = finder.fetchAttractions(radius, hotel);
            appendMessage(out, attractionName);
        } else {
            response.sendRedirect("/login.html");
        }
    }

    private void appendMessage(PrintWriter out, List<String> attractionName) {
        JsonObject gs = new JsonObject();
        JsonArray attArr = new JsonArray();
        int rec = 1;
        for (String name : attractionName) {
            if (rec <= 5) {
                JsonObject gs1 = new JsonObject();
                gs1.addProperty("Attraction", name);
                attArr.add(gs1);
                rec++;
            }
        }
        gs.add("Message", attArr);
        out.print(gs.toString());
    }
}