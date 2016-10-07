package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 10/2/15.
 */
public class ReportControlParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new DataPacket(packet[2] & 0xFF);
    }
}
