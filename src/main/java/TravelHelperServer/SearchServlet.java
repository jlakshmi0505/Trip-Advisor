package TravelHelperServer;

import DatabaseHandler.DatabaseUtil;
import hotelapp.Hotel;
import hotelapp.ThreadSafeHotelData;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SearchServlet extends TravelHelperBaseServlet {
    DatabaseUtil db = DatabaseUtil.getInstance();

    /**
     * This method will be used to display welcome page to the user
     * @param request
     * @param response
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            ve.init();
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("resource/myAccount.html");
            StringWriter writer = new StringWriter();
            PrintWriter out = response.getWriter();
            String lastLoginDetails = db.getLastLoginDetails(user);
            if(lastLoginDetails == null){
                lastLoginDetails = "This is your first login";
            }
            List<String> cityNames = null;
            try {
                cityNames = db.getCitiesFromHotel();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            context.put("user",user);
            context.put("lastLoginDetails",lastLoginDetails);
            context.put("cityNames", cityNames);
            template.merge(context, writer);
            out.println(writer.toString());
        }
        else {
            response.sendRedirect("/login.html");
        }
    }

    /**
     * This method will be used for searching hotels with search query or drop down value
     * @param request
     * @param response
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String user = (String) session.getAttribute("user");
        if (user != null) {
            String search = request.getParameter("search1");
            String cities = request.getParameter("cities");
            VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
            ve.init();
            VelocityContext context = new VelocityContext();
            Template template = ve.getTemplate("resource/myAccount.html");
            StringWriter writer = new StringWriter();
            PrintWriter out = response.getWriter();
            List<Hotel> list = db.findHotelByNameAndCity(search, cities);
            Map<String,Integer> map = db.selectAvgRating();
            String lastLoginDetails = db.getLastLoginDetails(user);
            List<String> cityNames = null;
            try {
                cityNames = db.getCitiesFromHotel();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            context.put("cityNames", cityNames);
            context.put("list", list);
            context.put("user", user);
            context.put("lastLoginDetails", lastLoginDetails);
            template.merge(context, writer);
            out.println(writer.toString());
        }
     else {
        response.sendRedirect("/login.html");
    }
}}
