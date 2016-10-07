package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.LockPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 7/15/16.
 */
public class LockParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new LockPacket(packet);
    }
}
