package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.Frame;

/**
 * Created by andrco on 5/12/16.
 */
public class FrameParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new Frame(packet);
    }
}
