package com.theshopatvsp.levelandroidsdk.model;

import java.io.Serializable;

/**
 * Created by andrco on 6/17/16.
 */
public class LastLocation implements Serializable {
    private double latitude;
    private double longitude;
    private double accuracy;
    private double altitude;
    private long lastTimestamp;
    private String glassName;

    public LastLocation() {}
    public LastLocation(double latitude, double longitude, double accuracy, double altitude, long lastTimestamp, String glassName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.lastTimestamp = lastTimestamp;
        this.glassName = glassName;
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

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public String getGlassName() {
        return glassName;
    }

    public void setGlassName(String glassName) {
        this.glassName = glassName;
    }
}
