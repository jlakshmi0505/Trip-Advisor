package TravelHelperServer;

import DatabaseHandler.DatabaseUtil;
import DatabaseHandler.Status;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LoginServlet extends TravelHelperBaseServlet {
    DatabaseUtil userDb = DatabaseUtil.getInstance();

    /** This method will be invoked when we make try to login from the main page
     * Here query parameter will be validated username hotelId and password
     * and if any of them is not present in the parameter then error json response will be send
     * @param request HttpRequest
     * @param response HttpServletResponse
     * @throws IOException Exception
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        username = StringEscapeUtils.escapeHtml4(username);
        String password = request.getParameter("password");
        password = StringEscapeUtils.escapeHtml4(password);
        Status status = userDb.authenticateUser(username, password);
        try {
            if (status == Status.OK) {
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                addLastLoginDetails(session);
                response.sendRedirect(response.encodeRedirectURL("/search"));
            } else {
                response.sendRedirect(response.encodeRedirectURL("/login.html?error=" + status.ordinal()));
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Adding Last login details to the user..
     * @param session Http session
     */
    private void addLastLoginDetails(HttpSession session) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = localDateTime.toLocalDate();
        LocalTime localTime = localDateTime.toLocalTime();
        session.setAttribute("lastLoginDate",localDate);
        session.setAttribute("lastLoginTime",localTime);
    }

    /**
     * This method will be called in http get request and it will be redirected to login page
     * @param request
     * @param response
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String error = request.getParameter("error");
        int code = 0;
        if (error != null) {
            try {
                code = Integer.parseInt(error);
            } catch (Exception ex) {
                code = -1;
            }
            String errorMessage = getStatusMessage(code);
            out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
        }
        response.sendRedirect(response.encodeRedirectURL("/login.html"));
    }

}
