package com.theshopatvsp.levelandroidsdk.ble.model.response;

/**
 * Created by andrco on 9/28/16.
 */
public class NukeRecordsPacket extends DataPacket {
    private int payload;

    public NukeRecordsPacket(int payload) {
        this.payload = payload;
    }

    public NukeRecordsPacket(byte[] packet) {
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
