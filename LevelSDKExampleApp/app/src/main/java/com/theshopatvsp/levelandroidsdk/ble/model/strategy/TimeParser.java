package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TimePacket;

/**
 * Created by andrco on 10/9/15.
 */
public class TimeParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        if( packet != null && packet.length == 6)
            return new TimePacket(packet);

        return null;
    }
}
