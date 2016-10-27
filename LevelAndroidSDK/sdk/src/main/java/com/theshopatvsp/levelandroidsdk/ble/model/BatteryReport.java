package com.theshopatvsp.levelandroidsdk.ble.model;

import java.io.Serializable;

/**
 * Created by andrco on 6/12/16.
 */
public class BatteryReport implements Serializable {
    private int percentage;
    private int voltage;
    private long timestamp;
    private String timezone;
    private long recordId;

    public BatteryReport() {}
    public BatteryReport(int percentage, int voltage, long timestamp, String timezone, long recordId) {
        this.percentage = percentage;
        this.voltage = voltage;
        this.timestamp = timestamp;
        this.timezone = timezone;
        this.recordId = recordId;
    }

    public int getPercentage() {
        return percentage;
    }

    public int getVoltage() {
        return voltage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public long getRecordId() {
        return recordId;
    }

    public void setRecordId(long recordId) {
        this.recordId = recordId;
    }
}
