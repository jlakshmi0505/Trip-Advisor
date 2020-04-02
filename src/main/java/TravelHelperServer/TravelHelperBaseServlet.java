package TravelHelperServer;

import DatabaseHandler.Status;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class TravelHelperBaseServlet extends HttpServlet {
    protected void prepareResponse(String title, HttpServletResponse response) {
        try {
            PrintWriter writer = response.getWriter();
            writer.printf("<!DOCTYPE html>%n%n");
            writer.printf("<html lang=\"en\">%n%n");
            writer.printf("<head>%n");
            writer.printf("\t<title>%s</title>%n", title);
            writer.printf("\t<meta charset=\"utf-8\">%n");
            writer.printf("</head>%n%n");
            writer.printf("<body>%n%n");
        }
        catch (IOException ex) {
            return;
        }
    }


    protected String getStatusMessage(String errorName) {
        Status status = null;
        try {
            status = Status.valueOf(errorName);
        } catch (Exception ex) {
            status = Status.ERROR;
        }
        return status.toString();
    }

    protected String getStatusMessage(int code) {
        Status status = null;
        try {
            status = Status.values()[code];
        } catch (Exception ex) {
            //   log.debug(ex.getMessage(), ex);
            status = Status.ERROR;
        }
        return status.toString();
    }
}
