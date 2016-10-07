package com.theshopatvsp.levelandroidsdk.ble.model;

import java.io.Serializable;

/**
 * Created by andrco on 6/12/16.
 */
public class Step implements Serializable {
    private int steps;
    private double mets;
    private long timestamp;
    private String timeZone;
    private double activeBurn;
    private int activeTime;
    private double distance;
    private long recordId;
    private long deviceTimestamp;
    private long originalTimestamp;


    public Step() {}

    public Step(int steps, long timestamp, String timeZone, double activeBurn, int activeTime, double distance, long deviceTimestamp, long originalTimestamp, long recordId) {
        this.steps = steps;
        this.timestamp = timestamp;
        this.timeZone = timeZone;
        this.activeBurn = activeBurn;
        this.activeTime = activeTime;
        this.distance = distance;
        this.deviceTimestamp = deviceTimestamp;
        this.originalTimestamp = originalTimestamp;
        this.recordId = recordId;
    }

    public Step(int total, long timestamp, String timezone) {
        this.steps = total;
        this.timestamp = timestamp;
        this.timeZone = timezone;
    }

    public int getSteps() {
        return steps;
    }

    public double getMets() {
        return mets;
    }

    public void setMets(double mets) {
        this.mets = mets;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public double getActiveBurn() {
        return activeBurn;
    }

    public int getActiveTime() {
        return activeTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setActiveBurn(double activeBurn) {
        this.activeBurn = activeBurn;
    }

    public void setActiveTime(int activeTime) {
        this.activeTime = activeTime;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Step && timestamp == ((Step) obj).timestamp;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }

    public long getDeviceTimestamp() {
        return deviceTimestamp;
    }

    public void setDeviceTimestamp(long deviceTimestamp) {
        this.deviceTimestamp = deviceTimestamp;
    }

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(long originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }
}
