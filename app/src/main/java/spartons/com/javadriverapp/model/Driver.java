package spartons.com.javadriverapp.model;

import android.support.annotation.NonNull;

public class Driver {

    private double lat;
    private double lng;
    private String driverId;

    public Driver(double lat, double lng, String driverId) {
        this.lat = lat;
        this.lng = lng;
        this.driverId = driverId;
    }

    public void updateDriver(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Driver() {
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Driver{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", driverId='" + driverId + '\'' +
                '}';
    }
}
