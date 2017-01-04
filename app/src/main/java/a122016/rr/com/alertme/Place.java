package a122016.rr.com.alertme;

/**
 * Created by rachitrawat on 1/4/2017.
 */

public class Place {

    private String placeOfAccident;
    private String highwayNumber;
    private int fatalties2015;
    private int fatalties2016;
    private String causeOfAccident;
    private double latitude;
    private double longitude;

    public Place(String placeOfAccident, String highwayNumber, int fatalties2015, int fatalties2016, String causeOfAccident, double latitude, double longitude) {
        this.placeOfAccident = placeOfAccident;
        this.highwayNumber = highwayNumber;
        this.fatalties2015 = fatalties2015;
        this.fatalties2016 = fatalties2016;
        this.causeOfAccident = causeOfAccident;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceOfAccident() {
        return placeOfAccident;
    }

    public void setPlaceOfAccident(String placeOfAccident) {
        this.placeOfAccident = placeOfAccident;
    }

    public String getHighwayNumber() {
        return highwayNumber;
    }

    public void setHighwayNumber(String highwayNumber) {
        this.highwayNumber = highwayNumber;
    }

    public int getFatalties2015() {
        return fatalties2015;
    }

    public void setFatalties2015(int fatalties2015) {
        this.fatalties2015 = fatalties2015;
    }

    public int getFatalties2016() {
        return fatalties2016;
    }

    public void setFatalties2016(int fatalties2016) {
        this.fatalties2016 = fatalties2016;
    }

    public String getCauseOfAccident() {
        return causeOfAccident;
    }

    public void setCauseOfAccident(String causeOfAccident) {
        this.causeOfAccident = causeOfAccident;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
