package hotelapp;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class responsible for getting tourist attractions near each hotel from the Google Places API.
 *  Also scrapes some data about hotels from expedia html webpage.
 */
public class TouristAttractionFinder {

    private static final String host = "maps.googleapis.com";
    private static final String path = "/maps/api/place/textsearch/json";
    private static final String query = "https://"+host+path+"?query=tourist%20attractions+in";
    private static final String ASTERISKS = "++++++++++++++++++++";
    private static final double MILES_TO_METRES = 1609.344;
    private ThreadSafeHotelData hotelData;

    /** Constructor for TouristAttractionFinder
     * @param hdata ThreadSafeHotelData object
     */
    public TouristAttractionFinder(ThreadSafeHotelData hdata) {
        this.hotelData = hdata;
    }

    public List<String> fetchAttractions(String radiusInMiles, Hotel hotel) {
        List<String> list = new ArrayList<>();
        JsonObject jsonObject = null;
            String apiKey = getAPIKey();
            if (apiKey != null && apiKey.length() > 0) {
                int radius = radiusInMiles != null && !radiusInMiles.isEmpty() ? Integer.parseInt(radiusInMiles):2;
                String jsonStringWithHeader = getJsonString(radius, hotel, apiKey);
                String jsonStringWithoutHeader = removeHeaders(jsonStringWithHeader);
                JsonElement ele = new JsonParser().parse(jsonStringWithoutHeader);
                jsonObject = ele.getAsJsonObject();
                list = parseTouristAttractionJson(jsonObject,hotel.getId());
            } else {
                System.out.println("Please provide API key!!!!!!!!");
            }
        return list;
    }

    public List<String> parseTouristAttractionJson(JsonObject json, String hotelId) {
        List<String> list = new ArrayList<>();
        if (json != null) {
            JsonArray ar = json.getAsJsonArray("results");
            for (int i = 0; i < ar.size(); i++) {
                JsonObject jsonObject1 = ar.get(i).getAsJsonObject();
                String name = jsonObject1.get("name").getAsString();
                list.add(name);
            }
        }
        return list;
    }



    /**
     * This method will make get request call to the api with the radius value passed
     *
     * @param radiusInMiles radius in miles
     * @param h             Hotel obj
     * @param apiKey        api key
     * @return json String
     */
    private String getJsonString(int radiusInMiles, Hotel h, String apiKey) {
        double radiusInMetres = radiusInMiles * MILES_TO_METRES;
        String s = "";
        String urlString = query + URLEncoder.encode(h.getCi(), StandardCharsets.UTF_8) + "&location=" + h.getLat()+","+h.getLng()+"&radius="+radiusInMetres+"&key="+apiKey;
        URL url;
        PrintWriter out = null;
        BufferedReader in = null;
        SSLSocket socket = null;
        try {
            url = new URL(urlString);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            String request = getRequest(url.getHost(), url.getPath() + "?" + url.getQuery());
            out.println(request); // send a request to the server
            out.flush();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // use input stream to read server's response
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            s = sb.toString();
        } catch (IOException e) {
            System.out.println(
                    "An IOException occured while writing to the socket stream or reading from the stream: " + e);
        } finally {
            try {
                if (out != null && in != null) {
                    out.close();
                    in.close();
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("An exception occured while trying to close the streams or the socket: " + e);
            }
        }
        return s;
    }


    /** Will create getRequest for the url and host provided
     * @param host  host name
     * @param pathResourceQuery query passed
     * @return string
     */
    private String getRequest(String host, String pathResourceQuery) {
        return "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator()
                + "Host: " + host + System.lineSeparator()
                + "Connection: close" + System.lineSeparator()
                + System.lineSeparator();
    }


    /** This method will remove headers from the  response using regex
     * @param s String response with headers
     * @return string after removing headers from response
     */
    private String removeHeaders(String s) {
        String patternString1 = "(.*?)\\{(.*)";
        String json = "";
        Pattern pattern = Pattern.compile(patternString1);
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            json = "{" + matcher.group(2);
        }
        return json;
    }



    /** This method returns API key from the config.json file
     * @return string api key
     */
    private String getAPIKey() {
        String apiKey = "";
        try (JsonReader jsonReader = new JsonReader(new FileReader("input" + File.separator + "config.json"))) {
            JsonElement jsonElement = new JsonParser().parse(jsonReader).getAsJsonObject();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            apiKey = jsonObject.get("apikey").getAsString();
        } catch (IOException e) {
            System.out.println("IO Exception occurred while retrieving API key");
        }
        return apiKey;
    }

}