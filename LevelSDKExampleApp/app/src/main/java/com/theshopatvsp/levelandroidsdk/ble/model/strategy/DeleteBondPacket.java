package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 6/1/16.
 */
public class DeleteBondPacket extends DataPacket {
    private int payload;

    public DeleteBondPacket(int payload) {
        this.payload = payload;
    }

    public DeleteBondPacket(byte[] packet) {
        if (packet != null && packet.length > 0) {
            payload = packet[0];
        }
    }

    public byte[] getPacket() {
        byte bytes[] = new byte[1];

        bytes[0] = 22;

        return bytes;
    }

    public int getPayload() {
        return payload;
    }
}
