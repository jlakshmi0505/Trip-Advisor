package TravelHelperServer;
import DatabaseHandler.DatabaseUtil;
import DatabaseHandler.Status;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hotelapp.Hotel;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

public class VisitedHotelServlet extends HttpServlet {
    DatabaseUtil db = DatabaseUtil.getInstance();

    /**
     * This method will be invoked when we make get request call to jetty server
     * Here query parameter will be validated like hotelId and adding hotel list to list of save hotels
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
                deleteVisitedHotelList(out, user);
            } else {
                addVisitedHotelList(user, hotelId);
            }
        } else {
            response.sendRedirect("/login.html");
        }
    }

    /**
     * This method will retrieve list of visited hotel
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
            getListOfVisitedLinks(out, user);

        } else {
            response.sendRedirect("/login.html");
        }
    }

    private void addVisitedHotelList(String user, String hotelId) {
        if (hotelId != null) {
            hotelId = StringEscapeUtils.escapeHtml4(hotelId);
            Hotel hotel = db.findHotelById(hotelId);
            if(hotel != null){
                db.addHotelVisited(user,hotel.getF(),hotelId);
            }}
    }

    private void deleteVisitedHotelList(PrintWriter out, String user) {
        Status status = db.deleteVisitedHotelForUser(user);
        String message = "";
        if (status == Status.OK) {
            message = "Visited List Deleted Successfully";
        } else {
            message = "No Visited List for user";
        }
        JsonObject gs = new JsonObject();
        gs.addProperty("Message", message);
        out.println(gs.toString());
    }



    private void getListOfVisitedLinks(PrintWriter out, String user) {
        try {
            List<String> listOfHotelId = db.getListOfVisitedHotel(user);
            JsonObject gs = new JsonObject();
            JsonArray attArr = new JsonArray();
            for(String id : listOfHotelId){
                JsonObject gs1 = new JsonObject();
                gs1.addProperty("Hotel", id);
                attArr.add(gs1);
            }
        gs.add("Message", attArr);
        out.print(gs.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}