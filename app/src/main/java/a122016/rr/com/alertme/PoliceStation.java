package a122016.rr.com.alertme;

/**
 * Created by rachitrawat on 1/9/2017.
 */

public class PoliceStation {

    private String mName;
    private String mNumber;
    private double mLatitude;
    private double mLongitude;

    public PoliceStation(String mName, String mNumber, double mLatitude, double mLongitude) {
        this.mName = mName;
        this.mNumber = mNumber;
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmNumber(String mNumber) {
        this.mNumber = mNumber;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
