package TravelHelperServer;

import DatabaseHandler.DatabaseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;

public class LogOutServlet extends TravelHelperBaseServlet {
    DatabaseUtil db = DatabaseUtil.getInstance();

    /**
     * This method will be called by the user for logging out, it stores last login time in database and also invalidate the
     * session
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();
        if (session != null) {
            String user = (String)session.getAttribute("user");
            LocalDate lastLoginDate = (LocalDate) session.getAttribute("lastLoginDate");
            LocalTime lastLoginTime = (LocalTime) session.getAttribute("lastLoginTime");
            db.updateLastLogin(user,lastLoginDate,lastLoginTime);
            session.setAttribute("user",null);
            session.invalidate();
            out.println("<p>Successfully logged out.</p>");
            response.sendRedirect(response.encodeRedirectURL("/login.html"));
        }

    }
}
