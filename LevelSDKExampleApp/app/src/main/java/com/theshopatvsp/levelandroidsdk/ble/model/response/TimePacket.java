package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.*;

/**
 * Created by andrco on 10/9/15.
 */
public class TimePacket extends DataPacket {
    long timestamp;

    public TimePacket() {
        super();
    }

    public TimePacket(byte packet[]) {
        super();
        timestamp = BitsHelper.convetTo32BitLong(packet[5], packet[4], packet[3], packet[2]) * 1000;
    }

    public TimePacket(long timestamp) {
        super();

        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getPacket() {
        return BitsHelper.convertLongTo4Bytes(this.timestamp);
    }

    @Override
    public String toString() {
        return "TimePacket{" +
                "timestamp=" + timestamp +
                "} " + super.toString();
    }
}
