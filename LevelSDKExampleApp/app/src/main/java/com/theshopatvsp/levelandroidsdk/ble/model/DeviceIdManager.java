package com.theshopatvsp.levelandroidsdk.ble.model;

/**
 * Created by andrco on 6/11/16.
 */
public class DeviceIdManager {
    private int packetIdOut = 0;
    private int packetIdIn = -1;

    public int getPacketIdOut() {
        return packetIdOut;
    }

    public void incPacketIdOut() {
        this.packetIdOut++;

        if( this.packetIdOut > 255 ) {
            this.packetIdOut = 0;
        }
    }

    public int getPacketIdIn() {
        return packetIdIn;
    }

    public void setPacketIdIn(int packetIdIn) {
        this.packetIdIn = packetIdIn;
    }

    public void incPacketIdIn() {
        this.packetIdIn++;

        if (this.packetIdIn > 255) {
            this.packetIdIn = 0;
        }
    }

    public void reset() {
        this.packetIdIn = -1;
        this.packetIdOut = 0;
    }
}
