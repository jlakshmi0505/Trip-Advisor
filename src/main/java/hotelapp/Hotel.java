package hotelapp;

/**
 *  Hotel class that represents a hotel that has id,address,hotelName
 */
public class Hotel implements Comparable<Hotel> {
    public void setF(String f) {
        this.f = f;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public void setPr(String pr) {
        this.pr = pr;
    }

    private String f;
    private String id;
    private double lat;
    private double lng;
    private String ad;
    private String ci;

    public int getAvgRating() {
        return avgRating;
    }

    private int avgRating;


    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    private String pr;

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getAd() {
        return ad;
    }

    public String getCi() {
        return ci;
    }

    public String getPr() {
        return pr;
    }

    public Hotel(String id, String f, String ci, String pr, String ad, int avgRating,double lat, double lng) {
        this.f = f;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.ad = ad;
        this.ci = ci;
        this.pr = pr;
        this.avgRating = avgRating;
    }

    @Override
    public String toString() {
        return System.lineSeparator() +
                f + ":" + id +
                ad + ci + "," + pr;
    }

    public String getF() {
        return f;
    }

    public String getId() {
        return id;
    }

    /**
     * It will compare hotel ids
     * @param o Other Hotel Obj
     * @return positive if id of hotel is more that other hotel id
     */
    @Override
    public int compareTo(Hotel o) {
        if (id.compareTo(o.id) > 0) {
            return 1;
        } else if (id.compareTo(o.id) < 0) {
            return -1;
        }
        return 0;
    }
}


