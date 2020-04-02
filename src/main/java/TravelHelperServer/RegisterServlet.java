package TravelHelperServer;

import DatabaseHandler.DatabaseUtil;
import DatabaseHandler.Status;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterServlet extends TravelHelperBaseServlet {
    private DatabaseUtil userDb = DatabaseUtil.getInstance();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        username = StringEscapeUtils.escapeHtml4(username);
        String password = request.getParameter("password");
        password = StringEscapeUtils.escapeHtml4(password);
        if (username != null && password != null) {
            boolean validPass = validatePassword(password);
            if (validPass) {
                Status status = userDb.duplicateUser(username);
                validateRegisteredUser(response, username, password, status);
            }
        } else {
            String url = "/register.html";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }
    }

    /**
     * This method will validate registered user
     * @param response
     * @param username
     * @param password
     * @param status
     * @throws IOException
     */

    private void validateRegisteredUser(HttpServletResponse response, String username, String password, Status status) throws IOException {
        if (status == Status.OK) {
            status = userDb.registerUser(username, password);
            if (status == Status.OK) {
                response.sendRedirect(response.encodeRedirectURL("login.html"));
            }
        } else {
            PrintWriter out = response.getWriter();
            out.println("<p style=\"color: red;\">" + "Duplicate Username" + "</p>");
            String url = "/register.html";
            url = response.encodeRedirectURL(url);
            response.sendRedirect(url);
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String error = request.getParameter("error");
        if (error != null) {
            String errorMessage = getStatusMessage(error);
            out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
        }
        doPost(request, response);
    }

    /**
     * This method will validate password with regex
     * @param password
     * @return
     */
    private boolean validatePassword(String password) {
        Pattern  pattern = Pattern.compile("(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#\\$%\\^&]).{5,10}");
        Matcher matcher = pattern.matcher(password);
        if(matcher.matches()){
            return true;
        }
        return false;
    }

    protected String getStatusMessage(String errorName) {
        Status status = null;
        try {
            status = Status.valueOf(errorName);
        } catch (Exception ex) {
            //  log.debug(errorName, ex);
            status = Status.ERROR;
        }
        return status.toString();
    }
}
