package com.theshopatvsp.levelandroidsdk.ble.model;

import java.io.Serializable;

/**
 * Created by andrco on 6/13/16.
 */
public class AccelFilt implements Serializable {
    private long timestamp;
    private String timezone;
    private int reading;
    private long recordId;

    public AccelFilt() {}
    public AccelFilt(long timestamp, String timezone, int reading, long recordId) {
        this.timestamp = timestamp;
        this.timezone = timezone;
        this.reading = reading;
        this.recordId = recordId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getReading() {
        return reading;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setReading(int reading) {
        this.reading = reading;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }
}
