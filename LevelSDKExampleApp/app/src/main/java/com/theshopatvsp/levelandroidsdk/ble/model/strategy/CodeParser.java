package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.CodePacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.PacketParser;

/**
 * Created by andrco on 5/27/16.
 */
public class CodeParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new CodePacket(packet);
    }
}
