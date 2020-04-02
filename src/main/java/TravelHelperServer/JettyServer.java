package TravelHelperServer;
import hotelapp.ThreadSafeHotelData;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
public class JettyServer {
    private static final int PORT = 8050;
    public static void main(String[] args) throws Exception {
      /*  String dir = Paths.get(".", "/myDir").toString();
        ServletContextHandler context  = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setResourceBase(dir);
        context.addServlet(RegisterServlet.class, "/register"); // add other paths like that...
        ServletHolder holder = new ServletHolder("default", DefaultServlet.class);
        holder.setInitParameter("dirAllowed","false");
        context.addServlet(holder, "/");*/
        ThreadSafeHotelData data = new ThreadSafeHotelData();
       /* HotelDataBuilder builder = new HotelDataBuilder(data, 1);
        String inputHotelFile = "input" + File.separator + "hotels.json";
        builder.loadHotelInfo(inputHotelFile);
        builder.loadReviews(Paths.get("input" + File.separator + "reviews"));*/
        Server server = new Server(PORT);
        ServletContextHandler servHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servHandler.setContextPath("/");
        servHandler.addServlet(new ServletHolder(new RegisterServlet()), "/register");
        servHandler.addServlet(new ServletHolder(new LoginServlet()), "/login");
        servHandler.addServlet(new ServletHolder(new LogOutServlet()), "/logout");
        servHandler.addServlet(new ServletHolder(new SearchServlet()), "/search");
        servHandler.addServlet(new ServletHolder(new HotelServlet()), "/hotel");
        servHandler.addServlet(new ServletHolder(new ReviewServlet(data)), "/addReview");
        servHandler.addServlet(new ServletHolder(new AttractionsServlet(data)), "/attractions");
        servHandler.addServlet(new ServletHolder(new SaveHotelServlet()), "/saveHotel");
        servHandler.addServlet(new ServletHolder(new VisitedHotelServlet()), "/visitHotel");
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase("resource");
        VelocityEngine velocity = new VelocityEngine();
        velocity.init();
        servHandler.setAttribute("templateEngine", velocity);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, servHandler});
        server.setHandler(handlers);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            System.out.println("Exception occurred while running the server: " + e);
        }
    }
}
