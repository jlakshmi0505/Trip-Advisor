package TravelHelperServer;
import DatabaseHandler.DatabaseUtil;
import DatabaseHandler.Status;
import com.google.gson.JsonArray;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SaveHotelServlet extends HttpServlet {
    DatabaseUtil db = DatabaseUtil.getInstance();

    /**
     * This method will be invoked when we make get request call to jetty server
     * Here query parameter will be validated like hotelId
     * and if that is not present in the parameter then error json response will be send to the browser
     *
     * @param request  HttpRequest
     * @param response HttpServletResponse
     * @throws IOException Exception
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            ve.init();
            String hotelId = request.getParameter("hotelId");
            String delete = request.getParameter("delete");
            if (delete != null) {
                deleteSaveHotelList(out, user);
            } else {
                addingHotelsToSaveList(out, user, hotelId);
            }
        } else {
            response.sendRedirect("/login.html");
        }
    }

    private void deleteSaveHotelList(PrintWriter out, String user) {
        Status status = db.deleteSavedHotelForUser(user);
        String message = "";
        if (status == Status.OK) {
            message = "Saved List Deleted Successfully";
        } else{
            message = "No Saved List for user";
        }
        JsonObject gs = new JsonObject();
        gs.addProperty("Message", message);
        out.println(gs.toString());
    }

    private void addingHotelsToSaveList(PrintWriter out, String user, String hotelId) {
        if (hotelId != null) {
            hotelId = StringEscapeUtils.escapeHtml4(hotelId);
            Hotel hotel = db.findHotelById(hotelId);
            if (hotel != null) {
                Status status = db.saveHotelSelected(user, hotel.getF(), hotelId);
                String message;
                if (status == Status.OK) {
                    message = "Added hotel to saved list";
                } else {
                    message = "Hotel already added  to saved list";
                }
                JsonObject gs = new JsonObject();
                gs.addProperty("Message", message);
                out.println(gs.toString());
            }
        }
    }

    /**
     * This method will be used to retrieve list of saved hotels
     * @param request
     * @param response
     * @throws IOException
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
            try {
                retrieveListOfSaveHotel(out, user);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("/login.html");
        }
    }

    private void retrieveListOfSaveHotel(PrintWriter out, String user) throws SQLException {
        List<String> listOfHotelId = db.getListOfSavedHotel(user);
        JsonObject gs = new JsonObject();
        JsonArray attArr = new JsonArray();
        for (String name : listOfHotelId) {
            JsonObject gs1 = new JsonObject();
            gs1.addProperty("Hotel", name);
            attArr.add(gs1);
        }
        gs.add("Message", attArr);
        out.print(gs.toString());
    }
}